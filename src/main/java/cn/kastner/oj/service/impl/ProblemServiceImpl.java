package cn.kastner.oj.service.impl;

import cn.kastner.oj.constant.EntityName;
import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.exception.CommonException;
import cn.kastner.oj.exception.FileException;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.query.ProblemQuery;
import cn.kastner.oj.repository.ContestProblemRepository;
import cn.kastner.oj.repository.IndexSequenceRepository;
import cn.kastner.oj.repository.ProblemRepository;
import cn.kastner.oj.repository.TagRepository;
import cn.kastner.oj.service.FileUploadService;
import cn.kastner.oj.service.ProblemService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class ProblemServiceImpl implements ProblemService {

  private final ProblemRepository problemRepository;

  private final ContestProblemRepository contestProblemRepository;

  private final FileUploadService fileUploadService;

  private final TagRepository tagRepository;

  private final IndexSequenceRepository indexSequenceRepository;

  private final DTOMapper mapper;

  private final String uploadDirectory;

  @Autowired
  public ProblemServiceImpl(
      @Value("${upload.path}") String uploadDirectory,
      ProblemRepository problemRepository,
      ContestProblemRepository contestProblemRepository,
      FileUploadService fileUploadService,
      TagRepository tagRepository,
      IndexSequenceRepository indexSequenceRepository, DTOMapper mapper) {
    this.uploadDirectory = uploadDirectory;
    this.problemRepository = problemRepository;
    this.contestProblemRepository = contestProblemRepository;
    this.fileUploadService = fileUploadService;
    this.tagRepository = tagRepository;
    this.indexSequenceRepository = indexSequenceRepository;
    this.mapper = mapper;
  }

  @Override
  public List<ProblemDTO> findProblemNoCriteria(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "idx");
    List<Problem> problemList = problemRepository.findAll(pageable).getContent();
    return mapper.toProblemDTOs(problemList);
  }

  @Override
  public PageDTO<ProblemDTO> findProblemCriteria(
      Integer page, Integer size, ProblemQuery problemQuery) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "idx");
    Specification<Problem> ps =
        (root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          String title = problemQuery.getTitle();
          if (!CommonUtil.isNull(title)) {
              predicateList.add(criteriaBuilder.like(root.get("title").as(String.class), "%" + title + "%"));
          }

          String tags = problemQuery.getTags();
          List<String> tagIdList = new ArrayList<>();
          List<Tag> tagList = new ArrayList<>();
          if (null != tags && !"".equals(tags)) {
            for (String i : problemQuery.getTags().split(",")) {
              tagIdList.add(i);
            }
            tagList = tagRepository.findAllById(tagIdList);
          }
          if (!tagList.isEmpty()) {
            predicateList.add(criteriaBuilder.isMember(tagList, root.get("tagList")));
          }

          Boolean visible = problemQuery.getVisible();
          if (user == null || !CommonUtil.isAdmin(user) || (visible != null && visible && CommonUtil.isAdmin(user))) {
            predicateList.add(criteriaBuilder.equal(root.get("visible"), true));
          }

          Predicate[] p = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(p));
        };
    List<Problem> problemList = problemRepository.findAll(ps, pageable).getContent();
    long count = problemRepository.count(ps);

    return new PageDTO<>(page, size, count, mapper.toProblemDTOs(problemList));
  }

  @Override
  public ProblemDTO findProblemById(String id) throws ProblemException {
    Problem problem = problemRepository.findById(id)
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    return mapper.entityToDTO(problem);
  }

  @Override
  public ProblemDTO findProblemByTitle(String title) {
    return mapper.entityToDTO(problemRepository.findByTitle(title));
  }

  @Override
  public ProblemDTO create(ProblemDTO problemDTO) throws ProblemException {
    User user = UserContext.getCurrentUser();
    Problem problem = problemRepository.findByTitle(problemDTO.getTitle());
    if (problem != null) {
      throw new ProblemException(ProblemException.HAVE_SUCH_TITLE_PROBLEM);
    } else {
      problem = mapper.dtoToEntity(problemDTO);
      problem.setAuthor(user);
      problem.setTagList(validateTagList(problem));

      for (SampleIO sampleIO : problem.getSampleIOList()) {
        sampleIO.setProblem(problem);
      }

      String prefix =
          File.separator + "problems" + File.separator + problem.getId() + File.separator;
      try {
        problem.setTestData(fileUploadService.saveFile(problem.getTestData(), prefix));
      } catch (IOException e) {
        e.printStackTrace();
        throw new ProblemException(ProblemException.TEST_DATA_PATH_INVALID);
      }

      try {
        processTestcase(problem, problem.getSpecialJudged());
      } catch (FileException e) {
        throw new ProblemException(e);
      }
      problem.setTestData("testDataDirectory:" + problem.getTestData());
      IndexSequence sequence = indexSequenceRepository.findByName(EntityName.PROBLEM);
      problem.setIdx(sequence.getNextIdx());
      ProblemDTO dto = mapper.entityToDTO(problemRepository.save(problem));
      sequence.setNextIdx(problem.getIdx() + 1);
      indexSequenceRepository.save(sequence);
      return dto;
    }
  }

  @Override
  public ProblemDTO update(ProblemDTO problemDTO) throws ProblemException {
    User user = UserContext.getCurrentUser();

    String id = problemDTO.getId();
    String title = problemDTO.getTitle();
    Problem problem = problemRepository.findByTitle(title);
    if (problem != null && !problem.getId().equals(id)) {
      throw new ProblemException(ProblemException.HAVE_SUCH_TITLE_PROBLEM);
    }

    problem = mapper.dtoToEntity(problemDTO);
    problem.setId(id);
    problem.setAuthor(user);

    problem.setTagList(validateTagList(problem));

    for (SampleIO sampleIO : problem.getSampleIOList()) {
      sampleIO.setProblem(problem);
    }
    if (!problem.getTestData().contains("testDataDirectory")) {
      String prefix = File.separator + "problems" + File.separator + problem.getId() + File.separator;
      try {
//      String relativePath = ;
        problem.setTestData(fileUploadService.saveFile(problem.getTestData(), prefix));
      } catch (IOException e) {
        e.printStackTrace();
        throw new ProblemException(ProblemException.TEST_DATA_PATH_INVALID);
      }

      try {
        processTestcase(problem, problem.getSpecialJudged());
      } catch (FileException e) {
        throw new ProblemException(e);
      }
    }

    return mapper.entityToDTO(problemRepository.save(problem));
  }

  @Override
  public ProblemDTO delete(String id) throws ProblemException {
    Problem problem = problemRepository.findById(id)
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    if (!contestProblemRepository.findByProblem(problem).isEmpty()) {
      throw new ProblemException(ProblemException.PROBLEM_REFERENCED);
    }
    problemRepository.delete(problem);
    return mapper.entityToDTO(problem);
  }

  private List<Tag> validateTagList(Problem problem) {
    List<Tag> tagList = new ArrayList<>();
    for (Tag t : problem.getTagList()) {
      Optional<Tag> tagOptional = tagRepository.findByName(t.getName());
      if (tagOptional.isPresent()) {
        Tag tag = tagOptional.get();
        tag.setProblemCount(tag.getProblemCount() + 1);
        tagList.add(tagOptional.get());
      } else {
        Tag newTag = new Tag();
        newTag.setName(t.getName());
        newTag.setProblemCount(newTag.getProblemCount() + 1);
        tagList.add(newTag);
      }
    }
    return tagList;
  }

  private void processTestcase(Problem problem, Boolean specialJudge) throws FileException {
    String testcasePath = uploadDirectory + problem.getTestData();
    String destDirectoryPath = testcasePath.substring(0, testcasePath.lastIndexOf(File.separator));

    Map<String, Object> sizeCache = new HashMap<>();
    Map<String, String> md5Cache = new HashMap<>();
    Map<String, Object> testCaseInfo = new HashMap<>();

    List<File> fileList = CommonUtil.unzip(testcasePath);
    File[] files = new File[fileList.size()];
    files = fileList.toArray(files);
    for (File file : files) {
      long length = 0;
      String content = "";
      try (BufferedInputStream inputFile = new BufferedInputStream(new FileInputStream(file))) {
        int len = inputFile.available();
        byte[] middleArray = new byte[len];
        inputFile.read(middleArray, 0, len);
        byte[] inputFiletoBytes = new byte[len];
        int index = 0;
        for (int i = 0; i < len; i++) {
          if (middleArray[i] == 13) {
            if (i + 1 < len && middleArray[i + 1] == 10) {
              inputFiletoBytes[index++] = 10;
              i++;
            } else inputFiletoBytes[index++] = middleArray[i];
          } else {
            inputFiletoBytes[index++] = middleArray[i];
          }
        }
        for (int i = index - 1; i >= 0; i--) {
          if (inputFiletoBytes[i] == 10) {
            index--;
          } else break;
        }
        byte[] finalArray = new byte[index];
        for (int i = 0; i < index; i++) {
          finalArray[i] = inputFiletoBytes[i];
        }
        sizeCache.put(file.getName(), finalArray.length);
        content = new String(finalArray);
        if (file.getName().endsWith(".out")) {
          md5Cache.put(file.getName(), CommonUtil.md5(finalArray));
        }
      } catch (IOException | NoSuchAlgorithmException e) {
        e.printStackTrace();
        throw new FileException(CommonException.COMMON_EXCEPTION);
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        writer.write(content);
      } catch (IOException e) {
        e.printStackTrace();
        throw new FileException(CommonException.COMMON_EXCEPTION);
      }
    }

    testCaseInfo.put("spj", specialJudge);
    Map<String, Object> testCases = new HashMap<>();

    List<Map<String, Object>> info = new ArrayList<>();
    if (specialJudge) {
      int index = 0;
      for (File file : files) {
        Map<String, Object> data = new HashMap<>();
        data.put("input_name", file.getName());
        data.put("input_size", sizeCache.get(file.getName()));
        info.add(data);
        testCases.put("" + (index + 1), data);
        index++;
      }
    } else {
      int j = 0;
      for (int i = 0; i < files.length; i += 2) {
        Map<String, Object> data = new HashMap<>();
        data.put("stripped_output_md5", md5Cache.get(files[i + 1].getName()));
        data.put("input_size", sizeCache.get(files[i].getName()));
        data.put("output_size", sizeCache.get(files[i + 1].getName()));
        data.put("input_name", files[i].getName());
        data.put("output_name", files[i + 1].getName());
        info.add(data);
        testCases.put("" + (++j), data);
      }
    }
    testCaseInfo.put("test_cases", testCases);

    String jsonStr = JSONObject.toJSONString(testCaseInfo);
    File infoJson = new File(destDirectoryPath + File.separator + "info");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoJson))) {
      writer.write(jsonStr);
    } catch (IOException e) {
      e.printStackTrace();
      throw new FileException(CommonException.COMMON_EXCEPTION);
    }
  }
}

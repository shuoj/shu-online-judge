package cn.kastner.oj.service;

import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.dto.RecommendOptionDTO;

import java.util.List;

public interface RecommendService {
    List<ProblemDTO> recommend(RecommendOptionDTO recommendOptionDTO);
}

package cn.kastner.oj.constant;

import cn.kastner.oj.domain.Language;

import java.util.HashMap;
import java.util.Map;

public class LanguageConfig {

  private static final String[] DEFAULT_ENV = {
      "LANG=en_US.UTF-8", "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8"
  };
  private static final String[] PYTHON_3_ENV = {
      "PYTHONIOENCODING=UTF-8", "LANG=en_US.UTF-8", "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8"
  };
  private static final Object C_LANG = CLangConfig();
  private static final Object CPP_LANG = CPPLangConfig();
  private static final Object JAVA_LANG = JAVALangConfig();
  private static final Object PYTHON_2_LANG = PYTHON2LangConfig();
  private static final Object PYTHON_3_LANG = PYTHON3LangConfig();

  public static Object getLanguageConfig(Language language) {
    switch (language) {
      case C:
        return C_LANG;
      case CPP:
        return CPP_LANG;
      case JAVA:

      default:
        return new HashMap<String, String>();
    }
  }

  private static Map<String, Object> CLangConfig() {
    Map<String, Object> map = new HashMap<>();

    Map<String, Object> compile =
        getCompile(
            "main.c",
            "main",
            3000,
            5000,
            128 * 1024 * 1024,
            "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {src_path} -lm -o {exe_path}");
    Map<String, Object> run = getRun("{exe_path}", "c_cpp", DEFAULT_ENV);

    map.put("compile", compile);
    map.put("run", run);
    return map;
  }

  private static Map<String, Object> CPPLangConfig() {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> compile =
        getCompile(
            "main.cpp",
            "main",
            3000,
            5000,
            128 * 1024 * 1024,
            "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++11 {src_path} -lm -o {exe_path}");
    Map<String, Object> run = getRun("{exe_path}", "c_cpp", DEFAULT_ENV);

    map.put("compile", compile);
    map.put("run", run);
    return map;
  }

  private static Map<String, Object> JAVALangConfig() {
    Map<String, Object> map = new HashMap<>();
    map.put("name", "java");

    Map<String, Object> compile =
        getCompile(
            "Main.java",
            "Main",
            3000,
            5000,
            -1,
            "/usr/bin/javac {src_path} -d {exe_dir} -encoding UTF8");
    Map<String, Object> run =
        getRun(
            "/usr/bin/java -cp {exe_dir} -XX:MaxRAM={max_memory}k -Djava.security.manager -Dfile.encoding=UTF-8 -Djava.security.policy==/etc/java_policy -Djava.awt.headless=true Main",
            "",
            DEFAULT_ENV);
    map.put("compile", compile);
    map.put("run", run);
    return map;
  }

  private static Map<String, Object> PYTHON2LangConfig() {
    Map<String, Object> map = new HashMap<>();

    Map<String, Object> compile =
        getCompile(
            "solution.py",
            "solution.pyc",
            3000,
            5000,
            128 * 1024 * 1024,
            "/usr/bin/python -m py_compile {src_path}");
    Map<String, Object> run =
        getRun(
            "/usr/bin/python {exe_path}",
            "general",
            DEFAULT_ENV);
    map.put("compile", compile);
    map.put("run", run);
    return map;
  }

  private static Map<String, Object> PYTHON3LangConfig() {
    Map<String, Object> map = new HashMap<>();

    Map<String, Object> compile =
        getCompile(
            "solution.py",
            "__pycache__/solution.cpython-35.pyc",
            3000,
            5000,
            128 * 1024 * 1024,
            "/usr/bin/python3 -m py_compile {src_path}");
    Map<String, Object> run =
        getRun(
            "/usr/bin/python3 {exe_path}",
            "general",
            PYTHON_3_ENV);
    map.put("compile", compile);
    map.put("run", run);
    return map;
  }

  private static Map<String, Object> getCompile(
      String srcName,
      String exeName,
      Integer maxCpuTime,
      Integer maxRealTime,
      Integer maxMemory,
      String compileCommand) {
    Map<String, Object> c = new HashMap<>();
    c.put("src_name", srcName);
    c.put("exe_name", exeName);
    c.put("max_cpu_time", maxCpuTime);
    c.put("max_real_time", maxRealTime);
    c.put("max_memory", maxMemory);
    c.put("compile_command", compileCommand);
    return c;
  }

  private static Map<String, Object> getRun(String command, String seccompRule, Object env) {
    Map<String, Object> r = new HashMap<>();
    r.put("command", command);
    r.put("seccomp_rule", seccompRule);
    r.put("env", env);
    if (command.contains("java")) {
      r.put("memory_limit_check_only", 1);
    }
    return r;
  }
}

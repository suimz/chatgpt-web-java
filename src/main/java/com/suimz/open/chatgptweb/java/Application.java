package com.suimz.open.chatgptweb.java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Resource;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	@Resource
	private AppProperties appProperties;

	public static void main(String[] args) throws Exception {
		autoCreateConfigFile(args);
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) {
		if (StrUtil.isAllBlank(appProperties.getOpenaiApiKey(), appProperties.getOpenaiAccessToken())) {
			throw new RuntimeException("[ app.openai-api-key ] and [ app.openai-access-token ] configure at least one");
		}
	}

	/**
	 * Before the program starts, if the `--spring.config.additional-location` parameter is passed in,
	 * will judge whether the file exists, and if file does not exist, will automatically create and fill the template content.
	 *
	 * -- This is a move to let docker map out the default configuration file.
	 */
	private static void autoCreateConfigFile(String[] args) throws Exception {
		if (ObjectUtil.isEmpty(args)) return;
		String configFilePath = null;
		String additionalLocationPrefix = "--spring.config.additional-location=";
		for (String arg : args) {
			if (StrUtil.startWith(arg, additionalLocationPrefix)) {
				configFilePath = arg.substring(additionalLocationPrefix.length());
				break;
			}
		}
		if (StrUtil.isBlank(configFilePath)) return;
		if (!FileUtil.exist(configFilePath)) {
			log.info("Created config file: {}", configFilePath);
			FileUtil.writeFromStream(new ClassPathResource("application-app.properties").getInputStream(), configFilePath);
		}
	}
}

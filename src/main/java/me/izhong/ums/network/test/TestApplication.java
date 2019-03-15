package me.izhong.ums.network.test;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
		try {
			String sl = System.getProperty("showlog");
			if(StringUtils.equals("true",sl))
				ConfigBean.showLog = true;
		} catch (Exception e) {

		}

		SpringApplication.run(TestApplication.class, args);
	}

}

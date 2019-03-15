package me.izhong.ums.network.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
		boolean showlog = true;
		try {
			showlog = Boolean.valueOf(System.getProperty("showlog"));
		}catch (Exception e) {

		}
		ConfigBean.showLog = showlog;

		SpringApplication.run(TestApplication.class, args);
	}

}

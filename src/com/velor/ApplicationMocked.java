package com.velor;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationMocked {

	public static void main(String[] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:com/velor/spring/spring-config-tests.xml");

		context.getBean(VelorProcessor.class).run(args);

	}
}

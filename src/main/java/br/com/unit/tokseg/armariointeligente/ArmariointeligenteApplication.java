package br.com.unit.tokseg.armariointeligente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class ArmariointeligenteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArmariointeligenteApplication.class, args);
	}

}

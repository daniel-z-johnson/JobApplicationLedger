package self.exercise.jobapplicationledger;

import org.springframework.boot.SpringApplication;

public class TestJobapplicationledgerApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobapplicationledgerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

package FileUploadORDownload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties({FileStorageProperties.class})

public class FileAPIApp {

	public static void main(String[] args) {
		
		SpringApplication.run(FileAPIApp.class, args);

	}

}

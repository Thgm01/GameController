package controller.net;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceApplication extends Thread{

    @Override
    public void run()
    {
        SpringApplication.run(RestServiceApplication.class);
    }

}

package controller.net;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import data.states.AdvancedData;

public class AutoRefServlet extends HttpServlet {

    AdvancedData gameData = null;

    public AutoRefServlet() {
        gameData = new AdvancedData();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String requestUrl = request.getRequestURI();
        System.out.print(requestUrl);

        String json = "{\n";
        json += "\"remaining_time\": " + JSONObject.quote("" + gameData.getRemainingGameTime(true)) + ",\n";
        json += "}";
        response.getOutputStream().println(json);

    }

}
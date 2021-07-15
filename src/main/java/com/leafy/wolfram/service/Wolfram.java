package com.leafy.wolfram.service;

import com.linecorp.bot.model.message.TextMessage;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class Wolfram {

    @Autowired
    @Qualifier("wolframAppID")
    private String appID;

    public TextMessage simpleApi(String input) {
        // Create a factory for performing WAQuery objects
        WAEngine engine = new WAEngine();

        // Set up the property for WAEngine
        engine.setAppID(appID);
        engine.addFormat("plaintext");

        // Create the query
        WAQuery query = engine.createQuery();

        // Set the property of the query
        query.setInput(input);

        String out;

        try {
            /* Send the query to the Wolfram|Alpha server and get the result
               and parse the result to WAQueryResult
             */
            WAQueryResult result = engine.performQuery(query);

            if (result.isError())
                out = "Sorry, there is an error when contacting the API Server.\n" +
                        "Error Code: " + result.getErrorCode() +"\n" +
                        "Error Msgs: " + result.getErrorMessage();
            else if (!result.isSuccess())
                out = "Sorry, there isn't enough data to process a meaningful result.";
            else {
                // Fetch the result
                StringBuilder str = new StringBuilder();

                int validCount = 0;
                for (WAPod pod : result.getPods()) {
                    if (!pod.isError()) {
                        String title = pod.getTitle();
                        for (WASubpod subpod : pod.getSubpods()) {
                            StringBuilder getDetail = new StringBuilder();
                            for (Object element : subpod.getContents()) {
                                if (element instanceof WAPlainText) {
                                    String detail = ((WAPlainText) element).getText();
                                    if (!detail.isEmpty()) getDetail.append(detail).append("\n");
                                }
                            }
                            if (!getDetail.toString().isEmpty()) {
                                str.append("\n").append(title).append("\n").append(getDetail.toString());
                                validCount++;
                            }
                        }
                    }
                    if (validCount == 2) break;
                }

                out = str.toString().trim();
            }
        } catch (WAException e) {
            e.printStackTrace();
            out = "Sorry. an unexpected error has occurred. Please try again in a few minutes.";
        }
        return new TextMessage(out);
    }

    // This function is only as reference as LINE won't let publish image from local link :(
//    public Message imageApi(String input) {
//        // We don't use the Wolfram Library as we want to access the v1 of the API instead of v2
//
//        // Changeable Variable
//        int width = 800, font_size = 18, background = 0;
//        String layout = "labelbar", foreground="black", units = "metric";
//
//        try {
//            String url = "https://api.wolframalpha.com/v1/simple?i=" + URLEncoder.encode(input, "UTF-8") +
//                    "&appid=" + appID + "&width=" + width + "&fontsize=" + font_size + "&layout=" + layout +
//                    "&background=" + background + "&foreground=" + foreground + "&units=" + units;
//
//            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
//            conn.setRequestMethod("GET");
//            conn.connect();
//
//            if (conn.getResponseCode() == 501) {
//                String errorMsg = "Sorry, there isn't enough data to process a meaningful result.";
//                return new TextMessage(errorMsg);
//            }
//            return new ImageMessage(url, url);
//        } catch (IOException e) {
//            e.printStackTrace();
//            String errorMsg = "Sorry. an unexpected error has occurred. Please try again in a few minutes.";
//            return new TextMessage(errorMsg);
//        }
//    }

    public TextMessage completeApi(String input) {
        // Create a factory for performing WAQuery objects
        WAEngine engine = new WAEngine();

        // Set up the property for WAEngine
        engine.setAppID(appID);
        engine.addFormat("plaintext");
        engine.addPodState("Step-by-step solution");

        // Create the query
        WAQuery query = engine.createQuery();

        // Set the property of the query
        query.setInput(input);

        String out;

        try {
            /* Send the query to the Wolfram|Alpha server and get the result
               and parse the result to WAQueryResult
             */
            WAQueryResult result = engine.performQuery(query);

            if (result.isError())
                out = "Sorry, there is an error when contacting the API Server.\n" +
                        "Error Code: " + result.getErrorCode() +"\n" +
                        "Error Msgs: " + result.getErrorMessage();
            else if (!result.isSuccess())
                out = "Sorry, there isn't enough data to process a meaningful result.";
            else {
                // Fetch the result
                StringBuilder str = new StringBuilder();

                for (WAPod pod : result.getPods()) {
                    if (!pod.isError()) {
                        str.append("\n").append(pod.getTitle()).append(":\n");
                        for (WASubpod subpod : pod.getSubpods()) {
                            for (Object element : subpod.getContents()) {
                                if (element instanceof WAPlainText) {
                                    String detail = ((WAPlainText) element).getText();
                                    if (!detail.isEmpty()) str.append("- ").append(((WAPlainText) element).getText()).append("\n");
                                }
                            }
                        }
                    }
                }

                out = str.toString().trim();
            }
        } catch (WAException e) {
            e.printStackTrace();
            out = "Sorry. an unexpected error has occurred. Please try again in a few minutes.";
        }
        return new TextMessage(out);
    }
}

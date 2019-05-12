import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

public class qTestJSONData extends APIClient {
    private static String TOKEN_BEARER = "2b3b8e43-ea06-43f9-a555-c1fb898fd7d8";
    private static String PROJECT_IDS = "87920,87944,87945";
    private static String PROJECT_GROUP_NAME = "qTest Sample API projects";
    private static String USERS = "51814,5202";

    public static void main(String[] args) throws JSONException {
        Date dateFrom = parseJsonDate("2019-03-24T00:00:00+00:00");
        Date dateTo = parseJsonDate("2019-03-24T23:59:59+00:00");

//        getTestCasesFromProjects(getProjectList(PROJECT_IDS), getUsersList(USERS), TOKEN_BEARER, PROJECT_GROUP_NAME, dateFrom, dateTo);
        getTestCasesFromProjects(getProjectList(PROJECT_IDS), getUsersList(USERS), TOKEN_BEARER, PROJECT_GROUP_NAME);
        getAllTestRunsForProjectsAndUsers(getProjectList(PROJECT_IDS), getUsersList(USERS), TOKEN_BEARER, PROJECT_GROUP_NAME);
    }

    protected static void getTestCasesFromProjects(JSONArray qTestProjects, List<String> users, String bearerToken, String projectGroupName, Date... dates) throws JSONException {
        int testCasesNo = 0;

        for (int i = 0; i < qTestProjects.length(); i++) {
            int lastPageNoOfTests;
            int pageNo = 0;
            do {
                JSONArray result = getAuthorisedWebb(bearerToken)
                        .get("/api/v3/projects/" + qTestProjects.get(i) + "/test-cases")
                        .param("page", ++pageNo)
//                        .param("size", 20)
                        .asJsonArray()
                        .getBody();

                for (int n = 0; n < result.length(); n++) {
                    JSONObject object = result.getJSONObject(n);
                    String user = object.get("creator_id").toString();

                    if (!(dates.length == 0)) {
                        Date created_date = parseJsonDate(object.get("created_date").toString());
                        if (dates[0].compareTo(created_date) * created_date.compareTo(dates[1]) > 0 && users.contains(user)) {
                            testCasesNo++;
                        }
                    } else if (users.contains(user)) {
                        testCasesNo++;
                    }
                }
                lastPageNoOfTests = result.length();
            } while (lastPageNoOfTests > 0);
        }

        System.out.println(projectGroupName + ": overall no of test cases created by given users: " + testCasesNo);
    }

    protected static void getAllTestRunsForProjectsAndUsers(JSONArray qTestProjects, List<String> users, String bearerToken, String projectGroupName, Date... dates) throws JSONException {
        int executedTestRunsNo = 0;
        int passedTestRunsNo = 0;
        float passedRatio;

        for (int i = 0; i < qTestProjects.length(); i++) {
            JSONArray result = getAuthorisedWebb(bearerToken)
                    .get("/api/v3/projects/" + qTestProjects.get(i) + "/test-runs")
                    .param("expand", "descendants")
                    .asJsonArray()
                    .getBody();

            for (int n = 0; n < result.length(); n++) {
                JSONObject object = result.getJSONObject(n);
                String creator_id = object.get("creator_id").toString();

                if (users.contains(creator_id)) {
                    JSONArray propertiesArr = object.getJSONArray("properties");
                    JSONObject testRunStatusProperty = (JSONObject) propertiesArr.get(12);

                    String testRunStatus = testRunStatusProperty.get("field_value_name").toString();

                    if (!(dates.length == 0)) {
                        Date created_date = parseJsonDate(object.get("created_date").toString());
                        if (dates[0].compareTo(created_date) * created_date.compareTo(dates[1]) > 0 &&
                                (testRunStatus.equals("Passed"))) {
                            passedTestRunsNo++;
                        }

                        if (dates[0].compareTo(created_date) * created_date.compareTo(dates[1]) > 0 &&
                                (testRunStatus.equals("Passed") || testRunStatus.equals("Failed") || testRunStatus.equals("Blocked") || testRunStatus.equals("Incomplete") || testRunStatus.equals("N/A"))) {
                            executedTestRunsNo++;
                        }
                    } else {
                        if (testRunStatus.equals("Passed")) {
                            passedTestRunsNo++;
                        }

                        if (testRunStatus.equals("Passed") || testRunStatus.equals("Failed") || testRunStatus.equals("Blocked") || testRunStatus.equals("Incomplete") || testRunStatus.equals("N/A")) {
                            executedTestRunsNo++;
                        }
                    }
                }
            }
        }
        passedRatio = ((float) passedTestRunsNo / (float) executedTestRunsNo) * 100;
        BigDecimal bd = new BigDecimal(passedRatio).setScale(2, RoundingMode.HALF_UP);
        passedRatio = bd.floatValue();

        System.out.println(projectGroupName + ": total no of executed test runs: " + executedTestRunsNo);
        System.out.println(projectGroupName + ": total no of passed test runs: " + passedTestRunsNo);
        System.out.println("Passed ratio: " + passedRatio + "%");
    }

}

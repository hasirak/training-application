package no.hials.trainingapp.routes;

import java.sql.SQLException;
import no.hials.trainingapp.auth.Auth;
import no.hials.trainingapp.datasource.DataItem;
import no.hials.trainingapp.datasource.DataSource;
import no.hials.trainingapp.routing.FormRoute;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import static spark.Spark.halt;

/**
 * Login a user
 *
 * @author Per Myren <progrper@gmail.com>
 */
public class Login extends FormRoute
{

    private final static String TEMPLATE_NAME = "login";

    // Used to cache the user object so we don't have to reload it again
    // after checking the username and password
    private DataItem mUserCache = null;

    public Login(DataSource datasource, Request req, Response resp)
    {
        super(datasource, req, resp);
    }

    @Override
    public ModelAndView get() throws Exception
    {
        checkUserNotAuthenticated();

        return renderTemplate(TEMPLATE_NAME);
    }

    @Override
    public ModelAndView post() throws Exception
    {
        checkUserNotAuthenticated();
        checkUsernameAndPassword();

        if (!hasValidationErrors()) {

            Auth.loginUser(getRequest(),
                           mUserCache.getInteger("customer_id"),
                           mUserCache.getString("customer_first_name"));

            getResponse().redirect("/");
            halt();
        }

        return renderTemplate(TEMPLATE_NAME);
    }

    /**
     * If user is already logged in there is no reason for him to log in so
     * redirect to the index page
     */
    private void checkUserNotAuthenticated()
    {
        if (getCurrentUser().isAuthenticated()) {
            getResponse().redirect("/");
            halt();
        }
    }

    /**
     * Check that the username and password is valid
     *
     * FIXME: Currently not checking password.
     *
     * @throws SQLException
     */
    private void checkUsernameAndPassword() throws SQLException
    {

        String username = getRequest().queryParams("username");

        DataItem customer = getDataSource().getCustomerByUsername(username);

        if (customer == null || !username.equals(customer.get("customer_email"))) {
            addValidationError("Wrong username or password");
        }
        else {
            mUserCache = customer;
        }

    }

}

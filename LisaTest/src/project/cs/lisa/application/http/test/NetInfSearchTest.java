package project.cs.lisa.application.http.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.http.NetInfResponse;
import project.cs.lisa.application.http.NetInfSearch;
import project.cs.lisa.application.http.NetInfSearchResponse;
import project.cs.lisa.application.http.NetInfStatus;
import project.cs.lisa.application.http.RequestFailedException;
import project.cs.lisa.mock.MockServer;
import android.test.InstrumentationTestCase;

/**
 * Test cases for NetInfSearch
 * @author Thiago Costa Porto
 */
public class NetInfSearchTest extends InstrumentationTestCase {

    public static final String TAG = "NetInfSearchTest";

    private static final int TIMEOUT_SECONDS = 5;

    private static final String mMsgId = "189371278363-123893712";
    private static final String mTokens = "lingus";
    private static final String mExt = "";

    private MockServer mMockServer;

    /**
     * Mock Server. SuppressedWarnings because... I can.
     * @author Thiago Costa Porto
     */
    public static class MockServerResource extends ServerResource {
        @SuppressWarnings("unchecked")
        @Get
        public String mockSearch() {
            if (getQuery().getFirstValue("tokens").equals("BADDYBAO")) {
                JSONObject json = new JSONObject();
                return json.toJSONString();
            }
            JSONObject json = new JSONObject();
            json.put("NetInf", "v.01a");
            json.put("msgid", mMsgId);
            json.put("status", 200);
            json.put("ts", "2012-11-07T10:02:15+00:00");
            JSONArray results = new JSONArray();
            results.put("bacon");
            json.put("results", results);
            return json.toJSONString();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Setup HTTP Server
        mMockServer = new MockServer();
        mMockServer.attach("/search", MockServerResource.class);

        assertNotNull("Failed to create mock server", mMockServer);

        mMockServer.start();

        assertTrue("Server should be started", mMockServer.isStarted());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Shutdown server
        mMockServer.stop();
        assertTrue("Server should be stopped", mMockServer.isStopped());
    }

    /**
     * Tests a successful search request.
     * @throws Throwable
     */
    public void testSearchSuccess() throws Throwable {
        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create search
        final NetInfSearch search = new NetInfSearch(mTokens, mExt) {
            @Override
            protected void onPostExecute(NetInfResponse response) {

                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a search response", response instanceof NetInfSearchResponse);
                NetInfSearchResponse searchResponse = (NetInfSearchResponse) response;
                assertEquals("Search should have succeeded",
                        searchResponse.getStatus(), NetInfStatus.OK);
                try {
                    assertEquals("Search should have returned bacon", (String) searchResponse.getSearchResults().get(0), "bacon");
                } catch (RequestFailedException e) {
                    fail("The response should have contained search results");
                }
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.execute();
            }

        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public void testSearchTokenNotFound() throws Throwable {
        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create search
        final NetInfSearch search = new NetInfSearch("BADDYBAO", mExt) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a search response", response instanceof NetInfSearchResponse);
                assertFalse("Search should not have succeeded",
                        ((NetInfSearchResponse) response).getStatus() == NetInfStatus.OK);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.execute();
            }

        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }


    public void testSearchHostUnknown() throws Throwable {
        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create search
        final NetInfSearch search = new NetInfSearch(mTokens, mExt) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a search response", response instanceof NetInfSearchResponse);
                assertFalse("Search should not have succeeded",
                        ((NetInfSearchResponse) response).getStatus() == NetInfStatus.OK);
                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                search.execute();
            }

        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public void testSearchPortNotSet() throws Throwable {

    }

    public void testSearchPortInvalid() throws Throwable {

    }

    public void testSearchConnectionFailed() throws Throwable {

    }

    public void testSearchConnectionSuccessful() throws Throwable {

    }

    public void testSearchTokensNotEncoded() throws Throwable {

    }

    public void testSearchExtNotEncoded() throws Throwable {

    }

    public void testSearchMsgIdNotEncoded() throws Throwable {

    }

    public void testSearchNoHashReturn() throws Throwable {

    }

    public void testSearchBadMessageReturned() throws Throwable {

    }
}

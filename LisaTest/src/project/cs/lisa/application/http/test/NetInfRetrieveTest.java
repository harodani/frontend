package project.cs.lisa.application.http.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import project.cs.lisa.application.http.NetInfResponse;
import project.cs.lisa.application.http.NetInfRetrieve;
import project.cs.lisa.application.http.NetInfRetrieveResponse;
import project.cs.lisa.application.http.NetInfStatus;
import project.cs.lisa.application.http.RequestFailedException;
import project.cs.lisa.mock.MockServer;
import android.test.InstrumentationTestCase;

public class NetInfRetrieveTest extends InstrumentationTestCase {

    public static final String TAG = "NetInfRetrieveTest";

    private static final int TIMEOUT_SECONDS = 5;

    private static final String mHash = "hashThatExists";
    private static final String mWrongHash = "hashThatDoesNotExists";
    private static final String mFilePath = "/path/to/file";
    private static final String mContentType = "image/jpeg";

    private static final String mHost = "localhost";
    private static final String mHashAlg = "sha-256";

    private MockServer mMockServer;

    public static class MockServerResource extends ServerResource {
        @Get
        public String mockGet() {
            assertNotNull("Hash not in URI query", getQuery().getFirstValue("hash"));
            assertNotNull("Hash algorithm not in URI query", getQuery().getFirstValue("hashAlg"));
            if(getQuery().getFirstValue("hash").equals(mHash)) {
                JSONObject json = new JSONObject();
                json.put("path", mFilePath);
                json.put("ct", mContentType);
                return json.toJSONString();
            } else {
                setStatus(Status.SUCCESS_NO_CONTENT);
                return null;
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Setup HTTP Server
        mMockServer = new MockServer();
        mMockServer.attach("/retrieve", MockServerResource.class);
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

    public void testRetrieveFileWritingFailed() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        // Create retrieve
        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.PORT), mHashAlg, mHash) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a retrieve response", response instanceof NetInfRetrieveResponse);
                NetInfRetrieveResponse retrieveResponse = (NetInfRetrieveResponse) response;
                System.out.println(retrieveResponse.getStatus());
                assertEquals("Retrieve should have succeeded, but the file doesn't exist",
                        NetInfStatus.FILE_DOES_NOT_EXIST, retrieveResponse.getStatus());
                try {
                    retrieveResponse.getFile().getAbsolutePath();
                    fail("Should have thrown RequestFailedException since file shouldn't exist");
                } catch (RequestFailedException e) {
                    // Should throw exception since the file doesn't exist
                }

                // Signal done
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieve.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testRetrieveNonExistent() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.PORT), mHashAlg, mWrongHash) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a retrieve response", response instanceof NetInfRetrieveResponse);
                assertFalse("Retrieve should not have succeeded",
                        ((NetInfRetrieveResponse) response).getStatus() == NetInfStatus.OK);
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieve.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

    public void testWrongPort() throws Throwable {

        // Signal used to wait for ASyncTask
        final CountDownLatch signal = new CountDownLatch(1);

        final NetInfRetrieve retrieve = new NetInfRetrieve(mHost, Integer.toString(MockServer.WRONG_PORT), mHashAlg, mHash) {
            @Override
            protected void onPostExecute(NetInfResponse response) {
                assertNotNull("Should always receive a response", response);
                assertTrue("Response should be a retrieve response", response instanceof NetInfRetrieveResponse);
                assertFalse("Retrieve should not have succeeded",
                        ((NetInfRetrieveResponse) response).getStatus() == NetInfStatus.OK);
                signal.countDown();
            }
        };

        // Run on UI thread
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieve.execute();
            }
        });

        // Wait a few seconds for the done signal, if timeout fail
        assertTrue("Request took more than " + TIMEOUT_SECONDS + " seconds.",
                signal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }

}

package project.cs.netinfservice.netinf.access.rest.resources;

import java.util.List;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.search.DefinedQueryTemplates;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.restlet.resource.Get;

import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfservice.util.metadata.Metadata;
import android.util.Log;

public class SearchResource extends LisaServerResource {

    private static final String TAG = "SearchResource";

    private static final int TIMEOUT = 1000;

    private String mTokens;

    /** Implementation of DatamodelFactory, used to create and edit InformationObjects etc. **/
    private DatamodelFactory mDatamodelFactory;

    /** Node Connection, used to access the local NetInf node. **/
    private NetInfNodeConnection mNodeConnection;

    @Override
    protected void doInit() {
        super.doInit();
        Log.d(TAG, "doInit()");

        mTokens = getQuery().getFirstValue("tokens", true);

        Log.d(TAG, "mTokens = " + mTokens);

        mDatamodelFactory = getDatamodelFactory();
        mNodeConnection   = getNodeConnection();
    }

    @Get
    public String search() throws NetInfCheckedException {
        Log.d(TAG, "printf");
        try {
            Log.d(TAG, "" + DefinedQueryTemplates.URL);
            List<Identifier> results = mNodeConnection.performSearch(DefinedQueryTemplates.URL, new String[] { mTokens }, TIMEOUT);

            Log.d(TAG, "printf34");
            // TODO Lets return a proper JSONObject.toString instead of a hardcoded string!
            if (results.isEmpty()) {
                return "{\"results\":[]}";
            }
                Identifier identifier = results.get(0);
                Log.d(TAG, "printf2");

                JSONObject jsonObject = new JSONObject();
                Log.d(TAG, "printf1234");
                jsonObject.put("results", new JSONArray().add(identifierToJson(identifier)));
                Log.d(TAG, "print12397123f");


            return jsonObject.toString();

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;

    }

    private JSONObject identifierToJson(Identifier identifier) {

        String hash = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
        String hashAlg = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
        String meta = identifier.getIdentifierLabel(SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();

        // Construct a metadata
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ni", "ni://" + hashAlg + ";" + hash);
        jsonObject.put("status", 200);

        Metadata metadata = new Metadata(meta);
        jsonObject.put("meta", metadata.getJSONObject());

        return jsonObject;
    }
}

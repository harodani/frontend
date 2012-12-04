package project.cs.netinfservice.netinf.node.search;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.node.search.SearchController;
import project.cs.netinfservice.application.MainNetInfApplication;
import project.cs.netinfservice.database.IODatabase;

import com.google.inject.Inject;

public class SearchServiceSQLite implements SimpleSearchService {

    private DatamodelFactory mDatamodelFactory;
    private SearchServiceIdentityObject mIdentityObject;
    private IODatabase mDatabase;


    @Inject
    public SearchServiceSQLite(final DatamodelFactory datamodelFactory) {
        mDatamodelFactory = datamodelFactory;
        mDatabase = mDatabaseFactory.create(MainNetInfApplication.getAppContext());
    }

    @Override
    public void getByKeywords(String request, int searchID,
            SearchServiceIdentityObject searchServiceIdO,
            SearchController callback) {
        // TODO Auto-generated method stub

    }

    // TODO Make sure we can always return true here. Is the DB really ready?
    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public SearchServiceIdentityObject getIdentityObject() {
       if (mIdentityObject == null) {
          createIdentityObject();
       }
       return mIdentityObject;
    }

    private void createIdentityObject() {
       SearchServiceIdentityObject idO = mDatamodelFactory.createSearchServiceIdentityObject();
       idO.setIdentifier(mDatamodelFactory.createIdentifier());
       idO.setName("SearchServiceSQLLite");
       idO.setDescription("This Search Service can be used to search in the local SQLLite DB.");
       mIdentityObject = idO;
    }

    @Override
    public String describe() {
        return "the local SQLite DB";
    }



}

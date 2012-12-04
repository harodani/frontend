package project.cs.netinfservice.netinf.node.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.impl.events.SearchServiceResultEvent;
import project.cs.netinfservice.application.MainNetInfApplication;
import project.cs.netinfservice.database.DatabaseException;
import project.cs.netinfservice.database.IODatabase;
import project.cs.netinfservice.database.IODatabaseFactory;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;

import com.google.inject.Inject;

public class SearchServiceSQLite implements SearchService {

    private DatamodelFactory mDatamodelFactory;
    private SearchServiceIdentityObject mIdentityObject;
    private IODatabase mDatabase;

    @Inject
    public SearchServiceSQLite(final DatamodelFactory datamodelFactory, IODatabaseFactory databaseFactory) {
        mDatamodelFactory = datamodelFactory;
        mDatabase = databaseFactory.create(MainNetInfApplication.getAppContext());
    }

    @Override
    public void getByQueryTemplate(String type, List<String> url, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // type should be "url"
        // url should be a list of 1 url
        // searchId
        // searchIdentity
        // searchController

        // TODO Make this able to return Set<Identifier>
        // TODO Get hash algorithm from database, not hardcoded
        // TODO Make sure metadata is correctly added

        Set<Identifier> resultSet = new HashSet<Identifier>();
        try {

            SearchResult searchResult = mDatabase.searchIO(url.get(0));

            Identifier identifier = mDatamodelFactory.createIdentifier();

            // Add Hash
            IdentifierLabel hashLabel = mDatamodelFactory.createIdentifierLabel();
            hashLabel.setLabelName(SailDefinedLabelName.HASH_CONTENT.getLabelName());
            hashLabel.setLabelValue(searchResult.getHash());
            identifier.addIdentifierLabel(hashLabel);

            // Add Hash Algorithm
            IdentifierLabel hashAlgLabel = mDatamodelFactory.createIdentifierLabel();
            hashAlgLabel.setLabelName(SailDefinedLabelName.HASH_ALG.getLabelName());
            hashAlgLabel.setLabelValue("sha-256");
            identifier.addIdentifierLabel(hashAlgLabel);

            // Add Metadata
            IdentifierLabel metaLabel = mDatamodelFactory.createIdentifierLabel();
            metaLabel.setLabelName(SailDefinedLabelName.META_DATA.getLabelName());
            metaLabel.setLabelValue(searchResult.getMetaData().convertToString());
            identifier.addIdentifierLabel(metaLabel);

            // Add the result
            resultSet.add(identifier);

        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        searchController.handleSearchEvent(new SearchServiceResultEvent("search result of " + getIdentityObject().getName(), searchId,
                searchIdentity, resultSet));

    }

    @Override
    public void getBySPARQL(String query, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // NOT SUPPORTED
        searchController.handleSearchEvent(new SearchServiceResultEvent("search result of " + getIdentityObject().getName(), searchId,
                searchIdentity, new HashSet<Identifier>()));
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

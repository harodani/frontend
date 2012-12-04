package project.cs.netinfservice.netinf.node.search;

import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.node.search.SearchController;
import netinf.node.search.impl.events.SearchServiceErrorEvent;
import netinf.node.search.impl.events.SearchServiceResultEvent;

public interface SimpleSearchService {

    /**
     * One possibility to execute a search. Keywords separated by whitepace.
     * <p>
     * This way of initiating a search is more user-friendly than OpenNetInf.
     * <p>
     * The method has to inform the responsible {@link SearchController} about either its search results or an occurred error by
     * calling {@link SearchController#handleSearchEvent(netinf.node.search.impl.events.SearchEvent)} with a
     * {@link SearchServiceResultEvent} respectively a {@link SearchServiceErrorEvent}.
     *
     * @param keywords
     *           keywords separated by whitespace to be easrched for
     * @param searchID
     *           the search ID to pass back on callback
     * @param searchServiceIdO
     *           the SearchServiceIdentityObject to pass back on callback
     * @param callback
     *           the controller that is responsible (to which the SearchService*Event is sent)
     */
    void getByKeywords(final String request, final int searchID, final SearchServiceIdentityObject searchServiceIdO,
            final SearchController callback);

    /**
     * @return true iff the service is ready to process search requests
     */
    boolean isReady();

    /**
     * @return the SearchServiceIdentityObject which represents the identity of this service
     */
    SearchServiceIdentityObject getIdentityObject();

    /**
     * Returns a textual description ("I can search via " + describe())
     *
     * @return textual description
     */
    String describe();

}

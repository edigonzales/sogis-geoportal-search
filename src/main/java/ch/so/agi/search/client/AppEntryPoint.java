package ch.so.agi.search.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.LocalSuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore.SuggestionsHandler;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.tabs.Tab;
import org.dominokit.domino.ui.tabs.TabsPanel;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.DelayedTextInput;
import org.dominokit.domino.ui.utils.DelayedTextInput.DelayedAction;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.HasChangeHandlers.ChangeHandler;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;
import org.jboss.elemento.EventType;

//import org.dominokit.domino.ui.Typography.Paragraph;
//import org.dominokit.domino.ui.cards.Card;
//import org.dominokit.domino.ui.forms.LocalSuggestBoxStore;
//import org.dominokit.domino.ui.forms.SuggestBoxStore;
//import org.dominokit.domino.ui.forms.SuggestItem;
////import org.dominokit.domino.ui.forms.SuggestBox;
//import org.dominokit.domino.ui.grid.Column;
//import org.dominokit.domino.ui.grid.Row;
//import org.dominokit.domino.ui.grid.Row_12;
//import org.dominokit.domino.ui.style.Color;
//import org.dominokit.domino.ui.tabs.Tab;
//import org.dominokit.domino.ui.tabs.TabsPanel;
//import org.dominokit.domino.ui.themes.Theme;
//import org.dominokit.domino.ui.cards.Card;
//import org.dominokit.domino.ui.utils.TextNode;
//import org.jboss.gwt.elemento.core.Elements;
//import static org.jboss.gwt.elemento.core.Elements.b;
//import static org.jboss.gwt.elemento.core.Elements.div;
//import org.dominokit.domino.ui.utils.TextNode;
//import org.jboss.elemento.Elements;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.qos.logback.classic.Logger;
import ch.so.agi.search.shared.SettingsResponse;
import ch.so.agi.search.shared.SettingsService;
import ch.so.agi.search.shared.SettingsServiceAsync;
//import elemental2.core.Global;
//import elemental2.core.JsArray;
//import elemental2.dom.DomGlobal;
//import elemental2.dom.HTMLElement;
//import elemental2.dom.HTMLDivElement;
//import elemental2.dom.HTMLElement;
//import elemental2.dom.Response;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsMap;
import elemental2.core.JsNumber;
import elemental2.core.JsObject;
import elemental2.core.JsString;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.RequestInit;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Coordinate;
import ol.Extent;

//import jsinterop.base.Js;
//import jsinterop.base.JsPropertyMap;

public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);
    
    private String MY_VAR;
    // &limit=XX
    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    
//    private String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/SearchServer?sr=2056&limit=15&type=locations&origins=address,parcel&searchText=";

    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
    SuggestBox suggestBox;
    DropDownMenu suggestionsMenu;
    HTMLDivElement resultPanel;

    public void onModuleLoad() {
        settingsService.settingsServer(new AsyncCallback<SettingsResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("error: " + caught.getMessage());
            }

            @Override
            public void onSuccess(SettingsResponse result) {
                MY_VAR = (String) result.getSettings().get("MY_VAR");
                init();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void init() { 
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();

        SuggestBoxStore dynamicStore = new SuggestBoxStore() {
            private MissingSuggestProvider<String> missingValueProvider;
            private MissingEntryProvider<String> missingEntryProvider;

            @Override
            public void filter(String value, SuggestionsHandler suggestionsHandler) {
                if (value.trim().length() == 0) {
                    return;
                }
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded"); // CORS and preflight...
                requestInit.setHeaders(headers);
                
                // fetch(url, init) -> https://www.javadoc.io/doc/com.google.elemental2/elemental2-dom/1.0.0-RC1/elemental2/dom/RequestInit.html
                // abort -> https://github.com/react4j/react4j-flux-challenge/blob/b9b28250fd3f954c690f874605f67e2a24a7274d/src/main/java/react4j/sithtracker/model/SithPlaceholder.java
                DomGlobal.fetch(SEARCH_SERVICE_URL + value.trim().toLowerCase(), requestInit)
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {
                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    JsArray<?> results = Js.cast(parsed.get("results"));
                    for (int i = 0; i < results.length; i++) {
                        JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                        if (resultObj.has("feature")) {
                            JsPropertyMap feature = (JsPropertyMap) resultObj.get("feature");
                            String display = ((JsString) feature.get("display")).normalize();
                            
                            SearchResult searchResult = new SearchResult();
                            searchResult.setLabel(display);

//                          // TODO icon type depending on address and parcel ?
                            SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), Icons.ALL.place());
                            suggestItems.add(suggestItem);

                        }

//                        SearchResult searchResult = new SearchResult();
//                        searchResult.setLabel("Fubar");
//                        searchResult.setLabel(((JsString) attrs.get("label")).normalize());
//                        searchResult.setOrigin(((JsString) attrs.get("origin")).normalize());
//                        searchResult.setBbox(((JsString) attrs.get("geom_st_box2d")).normalize());
//                        searchResult.setEasting(((JsNumber) attrs.get("y")).valueOf());
//                        searchResult.setNorthing(((JsNumber) attrs.get("x")).valueOf());
                        
                    }
                    suggestionsHandler.onSuggestionsReady(suggestItems);
                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });
            }
            
            @Override
            public void find(Object searchValue, Consumer handler) {
                if (searchValue == null) {
                    return;
                }
                SearchResult searchResult = (SearchResult) searchValue;
                console.log(suggestBox.getInputElement().getTextContent());
                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getDisplay());
                handler.accept(suggestItem);
                console.log(suggestBox.getInputElement().element().innerHTML);
            }            
        };
        
        
        suggestBox = SuggestBox.create("Suche: Orte, Karten, Daten, ...", dynamicStore);

        Icon searchIcon = Icons.ALL.search();
        searchIcon.addClickListener(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                suggestionsMenu.close();
                suggestBox.unfocus(); 
                evt.stopPropagation();

                console.log("makeitso");
            }
        });
        searchIcon.element().style.cursor = "pointer";
        suggestBox.addLeftAddOn(searchIcon);
        
        Icon resetIcon = Icons.ALL.close();
        resetIcon.addClickListener(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                HTMLInputElement el =(HTMLInputElement) suggestBox.getInputElement().element();
                el.value = "";
                suggestBox.focus();                
            }
        });
        resetIcon.element().style.cursor = "pointer";
        suggestBox.addRightAddOn(resetIcon);
        
        suggestBox.setAutoSelect(false);
        suggestBox.setFocusColor(Color.RED);
        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");        
        suggestBox.focus();                
        suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());

        HTMLInputElement suggestBoxElement =(HTMLInputElement) suggestBox.getInputElement().element();
        suggestBoxElement.addEventListener("keydown", evt -> {
            KeyboardEvent keyboardEvent = Js.uncheckedCast(evt);
            String key = keyboardEvent.key.toLowerCase();
            
            console.log("suggestionsMenu: " + key);
            
            evt.stopPropagation();
            suggestionsMenu.close();
        });
        



        suggestBox.addSelectionHandler(new SelectionHandler() {
            @Override
            public void onSelection(Object value) {
                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                SearchResult result = (SearchResult) item.getValue();
                console.log("onSelection: " + result.getDisplay());
            }
        });
        

        

        
//        suggestBox.addSelectionHandler(new SelectionHandler() {
//            @Override
//            public void onSelection(Object value) {
//
////                loader.stop();
////                resetGui();
//
//                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
//                SearchResult result = (SearchResult) item.getValue();
//                console.log("onSelection: " + result.getDisplay());
//                
////                String[] coords = result.getBbox().substring(4,result.getBbox().length()-1).split(",");
////                String[] coordLL = coords[0].split(" ");
////                String[] coordUR = coords[1].split(" ");
////                Extent extent = new Extent(Double.valueOf(coordLL[0]).doubleValue(), Double.valueOf(coordLL[1]).doubleValue(), 
////                Double.valueOf(coordUR[0]).doubleValue(), Double.valueOf(coordUR[1]).doubleValue());
////                
////                double easting = Double.valueOf(result.getEasting()).doubleValue();
////                double northing = Double.valueOf(result.getNorthing()).doubleValue();
////                
////                Coordinate coordinate = new Coordinate(easting, northing);
////                sendCoordinateToServer(coordinate.toStringXY(3), null);
//                
//                // TODO: remove focus
//                // -> Tried a lot but failed. Ask the authors.
//                
//                
//                
//                resultPanel.style.visibility = "visible";
//            }
//        });
	
        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").add(suggestBox).element();
        body().add(div().id("searchPanel").add(div().id("suggestBoxDiv").add(suggestBox)));

        Tab tabAll = Tab.create("Alle".toUpperCase())
                .style()
                .setWidth("25%")
                .get();
        Tab tabPlaces = Tab.create("Orte".toUpperCase())
                .style()
                .setWidth("25%")
                .get();
        Tab tabMap = Tab.create("Karten".toUpperCase())
                .style()
                .setWidth("25%")
                .get();
        Tab tabData = Tab.create("Daten".toUpperCase())
                .style()
                .setWidth("25%")
                .get();

        HTMLElement tabsPanel = TabsPanel.create().setId("resultTabs")
            .setBackgroundColor(Color.WHITE)
            .setColor(Color.BLUE)
            .appendChild(tabAll)
            .appendChild(tabPlaces)
            .appendChild(tabMap.appendChild(span().textContent("N/A")))
            .appendChild(tabData)
            .element();

        resultPanel = div().id("resultPanel").add(tabsPanel).element();
        resultPanel.style.visibility = "hidden";
        
        body().add(resultPanel);


    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}
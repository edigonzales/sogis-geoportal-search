package ch.so.agi.geoportal.search.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore.SuggestionsHandler;
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
import ch.so.agi.geoportal.search.shared.SettingsResponse;
import ch.so.agi.geoportal.search.shared.SettingsService;
import ch.so.agi.geoportal.search.shared.SettingsServiceAsync;
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
        Theme theme = new Theme(ColorScheme.BLUE);
        theme.apply();

        SuggestBoxStore dynamicStore = new SuggestBoxStore() {
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
        suggestBox.setIcon(Icons.ALL.search());
        suggestBox.setAutoSelect(false);
        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());
        
//        suggestionsMenu.addEventListener("keydown", evt -> {
//            KeyboardEvent keyboardEvent = Js.uncheckedCast(evt);
//            String key = keyboardEvent.key.toLowerCase();
//            console.log("suggestionsMenu" + key);
//        });

        // Funktioniert nicht wenn Suggestionspanel offen ist.
//        suggestBox.addEventListener("keydown", evt -> {
//
//            KeyboardEvent keyboardEvent = Js.uncheckedCast(evt);
//            String key = keyboardEvent.key.toLowerCase();
//            console.log(key);
//            
//            suggestionsMenu.blur();
//            suggestBox.focus();
//            
//            if (key.equalsIgnoreCase("enter")) {
//                console.log("fubar");
//                suggestionsMenu.close();
//            }
//            
//        });
        
        
//        suggestBox.setOnEnterAction(new DelayedAction() {
//            @Override
//            public void doAction() {
//                console.log("enter pressed.");
//                suggestionsMenu.close();
//            }
//        });
//        
//        suggestBox.removeEventListener("enter", new EventListener() {
//
//            @Override
//            public void handleEvent(Event evt) {
//                // TODO Auto-generated method stub
//                
//            }
//            
//        });
        

        suggestBox.addSelectionHandler(new SelectionHandler() {
            @Override
            public void onSelection(Object value) {
                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                SearchResult result = (SearchResult) item.getValue();
                console.log("onSelection: " + result.getDisplay());
            }
        });
        
        suggestBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onValueChanged(Object value) {
                HTMLInputElement el =(HTMLInputElement) suggestBox.getInputElement().element();
                suggestBox.getSuggestionsMenu().close();
                console.log("onValueChanged: " + el.value);
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

        
        
        
        
//        TabsPanel tabsPanel = TabsPanel.create();

        
//        FlowPanel searchBoxContainer = new FlowPanel();
//        searchBoxContainer.setStyleName("searchBoxContainer");
//
//        HorizontalPanel searchPanel = new HorizontalPanel();
//        searchPanel.setStyleName("searchPanel");
//        searchPanel.setSpacing(5);
//
//        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();  
//        oracle.add("Cat");
//        oracle.add("Dog");
//        oracle.add("Horse");
//        oracle.add("Canary");
//        
//        SuggestBox suggestBox = new SuggestBox(oracle, new TextBox(), new CustomSuggestionDisplay());
//        suggestBox.setWidth("100%");
//        suggestBox.setAutoSelectEnabled(false);
//        
//        suggestBox.addValueChangeHandler(h -> {
//            GWT.log("addValueChangeHandler: " + h.getValue()); 
//        });
//        
//        suggestBox.addSelectionHandler(h -> {
//            GWT.log("addSelectionHandler: " + h.getSelectedItem().getReplacementString()); 
//            
//            searchPanel.setStyleName("resultSearchPanel");
//            //RootPanel.get().getElement().getStyle().setProperty("backgroundColor", "white");
//            //searchBoxContainer.getElement().getStyle().setProperty("backgroundColor", "#fafafa");
//            //searchBoxContainer.getElement().getStyle().setProperty("borderBottom", "1px solid #D9D9D9");
//
////            tabsPanel.addStyleName("gwt-TabPanel-Result");
//            //tabsPanel.style().setMarginLeft("0px", true);
//            //tabsPanel.style().setPaddingLeft("100px", true);
//            //tabsPanel.style().setPaddingRight("100px", true);
////            tabsPanel.style().setMarginRight("auto", true);
//            //tabsPanel.style().setWidth("100%", true);
//            //tabsPanel.style().setMaxWidth("100%", true);
//            
//        });
//        
//        suggestBox.addKeyDownHandler(h -> {
//           //GWT.log(((SuggestBox) h.getSource()).getValue()); 
//            if (h.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//                GWT.log("ENTER");
//                
//                
//                GWT.log(suggestBox.getText());
//                
//                
//            }
//        });
//        searchPanel.add(suggestBox);
//        searchBoxContainer.add(searchPanel);
//        
//        Button searchButton = new Button("Suche", new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				GWT.log("Button clicked.");
//			}
//        });
//        searchPanel.add(searchButton);
//        
//        
//        RootPanel.get().add(searchBoxContainer);

        
        // Domino UI
//        tabsPanel.setBackgroundColor(Color.GREY_LIGHTEN_5);
//        tabsPanel.style().setMarginLeft("auto", true);
//        tabsPanel.style().setMarginRight("auto", true);
//        tabsPanel.style().setWidth("500px", true);
//        tabsPanel.style().setMaxWidth("100%", true);

//        Tab tabAll = Tab.create("Alles");
//        tabAll.appendChild(b().textContent("Home Content")).appendChild(Paragraph.create("Fubar"));
//        Tab tabPlaces = Tab.create("Orte");
//        tabPlaces.appendChild(b().textContent("Home Content")).appendChild(Paragraph.create("Fubar"));
//        Tab tabMaps = Tab.create("Karten");
//        tabMaps.appendChild(b().textContent("Home Content")).appendChild(Paragraph.create("Fubar"));
//        Tab tabServices = Tab.create("Dienste");
//        tabServices.appendChild(b().textContent("Home Content")).appendChild(Paragraph.create("Fubar"));
//        Tab tabData = Tab.create("Daten");
//        tabData.appendChild(b().textContent("Home Content")).appendChild(Paragraph.create("Fubar"));
//        Tab tabPlr = Tab.create("ÖREB");
//        tabPlr.appendChild(b().textContent("Home Content")).appendChild(Paragraph.create("Fubar"));
//        
//        tabsPanel.appendChild(tabAll);
//        tabsPanel.appendChild(tabPlaces);
//        tabsPanel.appendChild(tabMaps);
//        tabsPanel.appendChild(tabServices);
//        tabsPanel.appendChild(tabData);
//        tabsPanel.appendChild(tabPlr);
//        
        //HTMLDivElement tabsPanelContainer = div().asElement();
        //tabsPanelContainer.setAttribute("id", "tabsPanelContainer");
        //tabsPanelContainer.appendChild(tabsPanel.asElement());
        
//        Elements.body().add(tabsPanelContainer);
       

        /*
        // GWT pure
        FlowPanel tabContainer = new FlowPanel();
        tabContainer.setStyleName("tabContainer");
        
        // searchContext

        //create contents for tabs of tabpanel
        Label label0 = new Label("    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        label0.setHeight("200");
        Label label1 = new Label("This is contents of TAB 1");
        label1.setHeight("200");
        Label label2 = new Label("This is contents of TAB 2");
        label2.setHeight("200");
        Label label3 = new Label("This is contents of TAB 3");
        label3.setHeight("200");
        Label label4 = new Label("This is contents of TAB 4");
        label3.setHeight("200");
        Label label5 = new Label("This is contents of TAB 5");
        label3.setHeight("200");

        //create titles for tabs
        String tabAllTitle = "Alles";
        String tabPlacesTitle = "Orte";
        String tabMapsTitle = "Karten";
        String tabServicesTitle = "Dienste";
        String tabDataTitle = "Daten";
        String tabPlrTitle = "ÖREB";

        //create tabs 
        tabPanel.add(label0, tabAllTitle);
        tabPanel.add(label1, tabPlacesTitle);
        tabPanel.add(label2, tabMapsTitle);
        tabPanel.add(label3, tabServicesTitle);
        tabPanel.add(label4, tabDataTitle);
        tabPanel.add(label5, tabPlrTitle);

        //select first tab
        tabPanel.selectTab(0);

        //set width if tabpanel
        //tabPanel.setWidth("400");

        // Add the widgets to the root panel.
        tabContainer.add(tabPanel);
//        RootPanel.get().add(tabContainer);
        */
        
    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}
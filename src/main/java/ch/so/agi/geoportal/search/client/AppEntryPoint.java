package ch.so.agi.geoportal.search.client;

//import org.dominokit.domino.ui.cards.Card;
//import org.dominokit.domino.ui.utils.TextNode;
//import org.jboss.gwt.elemento.core.Elements;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.so.agi.geoportal.search.shared.SettingsResponse;
import ch.so.agi.geoportal.search.shared.SettingsService;
import ch.so.agi.geoportal.search.shared.SettingsServiceAsync;
//import elemental2.dom.HTMLElement;

public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);
    
    private String MY_VAR;
    
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
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

    private void init() {                        
        /*
        HTMLElement controlsCard = Card.create().setId("controls").setWidth("500px")
                .appendChild(TextNode.of("Hallo Stefan.")).asElement();
        
        Elements.body().add(controlsCard);
        */
        
        TabPanel tabPanel = new TabPanel();

        
        
        FlowPanel searchPanel = new FlowPanel();
        searchPanel.setStyleName("searchPanel");
        
        FlowPanel searchBoxContainer = new FlowPanel();
        searchBoxContainer.setStyleName("searchBoxContainer");
        
        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();  
        oracle.add("Cat");
        oracle.add("Dog");
        oracle.add("Horse");
        oracle.add("Canary");
        
        SuggestBox suggestBox = new SuggestBox(oracle, new TextBox(), new CustomSuggestionDisplay());
        suggestBox.setWidth("100%");
        
        suggestBox.addValueChangeHandler(h -> {
            GWT.log("addValueChangeHandler: " + h.getValue()); 
        });
        
        suggestBox.addSelectionHandler(h -> {
            GWT.log("addSelectionHandler: " + h.getSelectedItem().getReplacementString()); 
            
            searchPanel.setStyleName("resultSearchPanel");
            RootPanel.get().getElement().getStyle().setProperty("backgroundColor", "white");
            searchBoxContainer.getElement().getStyle().setProperty("backgroundColor", "#F7F7F7");
            searchBoxContainer.getElement().getStyle().setProperty("borderBottom", "1px solid #D9D9D9");

            tabPanel.addStyleName("gwt-TabPanel-Result");

        });
        
        suggestBox.addKeyDownHandler(h -> {
           //GWT.log(((SuggestBox) h.getSource()).getValue()); 
            if (h.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                GWT.log("ENTER");
            }
        });
        searchPanel.add(suggestBox);
        searchBoxContainer.add(searchPanel);
        
        RootPanel.get().add(searchBoxContainer);

        
        FlowPanel tabContainer = new FlowPanel();
        tabContainer.setStyleName("tabContainer");
        
        // searchContext

        //create contents for tabs of tabpanel
        Label label0 = new Label("This is contents of TAB 0");
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
        RootPanel.get().add(tabContainer);
        
        
    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}
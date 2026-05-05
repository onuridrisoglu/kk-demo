package com.example.wizard;

import com.example.WizardLayout;
import com.example.data.Order;
import com.example.service.OrderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "shipping-address", layout = WizardLayout.class)
@PermitAll
public class ShippingAddressSection extends Section {

    private ComboBox<String> countrySelect;
    private TextArea address;
    private TextField postalCode, city;

    private Binder<Order> binder;

    @Autowired
    private transient OrderService service;

    @PostConstruct
    private void init() {
        System.out.println("Displaying shipment address section");
        createLayout();
        initBinder();
    }

    private void createLayout() {
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Margin.Bottom.XLARGE, LumoUtility.Margin.Top.MEDIUM);

        H3 header = new H3("Shipping address");
        header.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.FontSize.XXLARGE);

        countrySelect = new ComboBox<>("Country");
        countrySelect.setRequiredIndicatorVisible(true);
        countrySelect.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        address = new TextArea("Street address");
        address.setMaxLength(200);
        address.setRequiredIndicatorVisible(true);
        address.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        Div subSection = new Div();
        subSection.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexWrap.WRAP, LumoUtility.Gap.MEDIUM);

        postalCode = new TextField("Postal Code");
        postalCode.setRequiredIndicatorVisible(true);
        postalCode.setPattern("[\\d \\p{L}]*");
        postalCode.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        city = new TextField("City");
        city.setRequiredIndicatorVisible(true);
        city.addClassNames(LumoUtility.Flex.GROW, LumoUtility.Margin.Bottom.SMALL);

        subSection.add(postalCode, city);

        countrySelect.setItems(service.getCountries());

        Button next = new Button("Next");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(evt -> save());

        Button back = new Button("Back");
        back.addClickListener(evt -> getUI().ifPresent(ui -> ui.navigate(PersonalDetailsSection.class)));

        add(header, countrySelect, address, subSection, new HorizontalLayout(back, next));
    }

    private void initBinder() {
        binder = new Binder<>();
        binder.forField(postalCode).asRequired().bind(Order::getPostalCode, Order::setPostalCode);
        binder.forField(city).asRequired().bind(Order::getCity, Order::setCity);
        binder.forField(address).asRequired().bind(Order::getAddress, Order::setAddress);
        binder.forField(countrySelect).asRequired().bind(Order::getCountry, Order::setCountry);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Order order = new Order();
        Long orderId = (Long) ComponentUtil.getData(attachEvent.getUI(), "order-id");
        if (orderId != null) {
            order = service.getOrder(orderId);
        }
        binder.setBean(order);
    }

    private void save() {
        if (binder.validate().isOk()) {
            Order order = binder.getBean();
            order = service.save(order);
            ComponentUtil.setData(UI.getCurrent(), Long.class, order.getId());
            getUI().ifPresent(ui -> ui.navigate(PaymentInformationSection.class));
        }
    }
}

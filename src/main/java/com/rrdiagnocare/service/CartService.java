package com.rrdiagnocare.service;

import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.model.CartItem;
import com.rrdiagnocare.model.DiagnosticTest;
import com.rrdiagnocare.model.HealthPackage;
import com.rrdiagnocare.util.AppConstants;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** HttpSession-backed shopping cart for tests and packages. */
@Service
public class CartService {

    private final CatalogService catalogService;

    public CartService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        Object cart = session.getAttribute(AppConstants.SESSION_CART);
        if (cart instanceof List) {
            return (List<CartItem>) cart;
        }
        List<CartItem> fresh = Collections.synchronizedList(new ArrayList<>());
        session.setAttribute(AppConstants.SESSION_CART, fresh);
        return fresh;
    }

    /** Adds a test or package; prices are always read fresh from the database. Returns the added item's name. */
    public String add(HttpSession session, String type, long id) {
        List<CartItem> cart = getCart(session);
        CartItem item;
        if (AppConstants.ITEM_PACKAGE.equals(type)) {
            HealthPackage p = catalogService.getPackage(id);
            if (!p.isActive()) throw new BusinessException("This package is currently unavailable.");
            item = new CartItem(AppConstants.ITEM_PACKAGE, p.getId(), p.getName(), AppConstants.CATEGORY_PACKAGES,
                    p.getPrice(), p.getPreparationNotes(), new ArrayList<>(p.getTestIds()));
        } else if (AppConstants.ITEM_TEST.equals(type)) {
            DiagnosticTest t = catalogService.getTest(id);
            if (!t.isActive()) throw new BusinessException("This test is currently unavailable.");
            item = new CartItem(AppConstants.ITEM_TEST, t.getId(), t.getName(), t.getCategory(), t.getPrice(),
                    t.getPreparationNotes(), Collections.singletonList(t.getId()));
        } else {
            throw new BusinessException("Unknown item type.");
        }
        synchronized (cart) {
            for (CartItem existing : cart) {
                if (existing.getKey().equals(item.getKey())) {
                    throw new BusinessException("'" + item.getName() + "' is already in your cart.");
                }
            }
            cart.add(item);
        }
        session.setAttribute(AppConstants.SESSION_CART, cart); // re-set so session replication notices the change
        return item.getName();
    }

    public void remove(HttpSession session, String key) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(i -> i.getKey().equals(key));
        session.setAttribute(AppConstants.SESSION_CART, cart);
    }

    public void clear(HttpSession session) {
        getCart(session).clear();
        session.setAttribute(AppConstants.SESSION_CART, getCart(session));
    }

    public BigDecimal total(List<CartItem> cart) {
        synchronized (cart) {
            return cart.stream().map(CartItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /** Names of individual tests that are also included in a package in the same cart (duplicate spend warning). */
    public List<String> overlapWarnings(List<CartItem> cart) {
        List<String> warnings = new ArrayList<>();
        synchronized (cart) {
            Set<Long> packageTestIds = new LinkedHashSet<>();
            for (CartItem i : cart) {
                if (i.isPackage()) packageTestIds.addAll(i.getTestIds());
            }
            for (CartItem i : cart) {
                if (!i.isPackage() && packageTestIds.contains(i.getItemId())) {
                    warnings.add(i.getName());
                }
            }
        }
        return warnings;
    }
}

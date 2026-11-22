package com.rrdiagnocare.service;

import com.rrdiagnocare.dao.DiagnosticTestDao;
import com.rrdiagnocare.dao.HealthPackageDao;
import com.rrdiagnocare.exception.BusinessException;
import com.rrdiagnocare.exception.ResourceNotFoundException;
import com.rrdiagnocare.model.DiagnosticTest;
import com.rrdiagnocare.model.HealthPackage;
import com.rrdiagnocare.util.AppConstants;
import com.rrdiagnocare.util.ValidationUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/** Test & package catalog: public browsing and admin CRUD. */
@Service
public class CatalogService {

    private final DiagnosticTestDao testDao;
    private final HealthPackageDao packageDao;

    public CatalogService(DiagnosticTestDao testDao, HealthPackageDao packageDao) {
        this.testDao = testDao;
        this.packageDao = packageDao;
    }

    // ------------------------------------------------------------- public catalog

    public List<DiagnosticTest> searchTests(String category, String query) {
        if (AppConstants.CATEGORY_PACKAGES.equals(category)) {
            return Collections.emptyList();
        }
        return testDao.search(category, query, true);
    }

    public List<HealthPackage> searchPackages(String category, String query) {
        if (category != null && !category.isBlank() && !AppConstants.CATEGORY_PACKAGES.equals(category)) {
            return Collections.emptyList();
        }
        return packageDao.search(query, true);
    }

    public List<HealthPackage> featuredPackages() {
        return packageDao.findFeatured();
    }

    public List<DiagnosticTest> allTests(boolean activeOnly) {
        return testDao.findAll(activeOnly);
    }

    public List<HealthPackage> allPackages(boolean activeOnly) {
        return packageDao.findAll(activeOnly);
    }

    public DiagnosticTest getTest(long id) {
        return testDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("Test not found"));
    }

    public HealthPackage getPackage(long id) {
        return packageDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("Health package not found"));
    }

    public int countActiveTests() {
        return testDao.countActive();
    }

    public int countActivePackages() {
        return packageDao.countActive();
    }

    // ------------------------------------------------------------- admin: tests

    @Transactional
    public long saveTest(DiagnosticTest t) {
        validateTest(t);
        t.setCode(t.getCode().trim().toUpperCase());
        t.setName(t.getName().trim());
        if (testDao.existsByCode(t.getCode(), t.getId())) {
            throw new BusinessException("Test code '" + t.getCode() + "' already exists.");
        }
        if (t.getId() == null) {
            return testDao.insert(t);
        }
        getTest(t.getId());
        testDao.update(t);
        return t.getId();
    }

    private void validateTest(DiagnosticTest t) {
        if (ValidationUtil.isBlank(t.getCode()) || ValidationUtil.isBlank(t.getName())) {
            throw new BusinessException("Test code and name are required.");
        }
        if (!AppConstants.TEST_CATEGORIES.contains(t.getCategory())) {
            throw new BusinessException("Please choose a valid category.");
        }
        if (t.getPrice() == null || t.getPrice().signum() < 0) {
            throw new BusinessException("Price must be zero or more.");
        }
        if (t.getNormalMin() != null && t.getNormalMax() != null && t.getNormalMin().compareTo(t.getNormalMax()) > 0) {
            throw new BusinessException("Normal range minimum cannot be greater than maximum.");
        }
        if (t.getTurnaroundHours() <= 0) {
            t.setTurnaroundHours(24);
        }
        if (ValidationUtil.isBlank(t.getSampleType())) {
            t.setSampleType("Blood");
        }
    }

    @Transactional
    public void toggleTest(long id) {
        getTest(id);
        testDao.toggleActive(id);
    }

    @Transactional
    public void deleteTest(long id) {
        getTest(id);
        try {
            testDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("This test is part of a package or an existing booking and cannot be deleted. Deactivate it instead.");
        }
    }

    // ------------------------------------------------------------- admin: packages

    @Transactional
    public long savePackage(HealthPackage p, List<Long> testIds) {
        if (ValidationUtil.isBlank(p.getName())) {
            throw new BusinessException("Package name is required.");
        }
        if (p.getPrice() == null || p.getPrice().signum() <= 0) {
            throw new BusinessException("Package price must be greater than zero.");
        }
        List<Long> ids = testIds == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(testIds));
        if (ids.size() < 2) {
            throw new BusinessException("A health package must bundle at least 2 tests.");
        }
        List<DiagnosticTest> tests = testDao.findByIds(ids);
        if (tests.size() != ids.size()) {
            throw new BusinessException("One or more selected tests no longer exist.");
        }
        BigDecimal mrp = tests.stream().map(DiagnosticTest::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (p.getPrice().compareTo(mrp) > 0) {
            throw new BusinessException("Package price (Rs. " + p.getPrice() + ") must not exceed the sum of its tests (Rs. " + mrp + ").");
        }
        p.setName(p.getName().trim());
        if (packageDao.existsByName(p.getName(), p.getId())) {
            throw new BusinessException("A package named '" + p.getName() + "' already exists.");
        }
        if (p.getId() == null) {
            return packageDao.insert(p, ids);
        }
        getPackage(p.getId());
        packageDao.update(p, ids);
        return p.getId();
    }

    @Transactional
    public void togglePackage(long id) {
        getPackage(id);
        packageDao.toggleActive(id);
    }

    @Transactional
    public void deletePackage(long id) {
        getPackage(id);
        packageDao.delete(id);
    }
}

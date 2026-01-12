# NutriMap Test Data

This document contains sample test data for Users, Children, and Visits to test the NutriMap Android application.

---

## 1. USERS TEST DATA

| ID | Name | Email | Password | Role | Branch ID |
|----|------|-------|----------|------|-----------|
| 1 | Admin User | admin@nutrimap.com | admin123 | ADMIN | B001 |
| 2 | John Supervisor | john.super@nutrimap.com | super123 | SUPERVISOR | B001 |
| 3 | Sarah Supervisor | sarah.super@nutrimap.com | super123 | SUPERVISOR | B002 |
| 4 | Maria Worker | maria.worker@nutrimap.com | worker123 | FIELD_WORKER | B001 |
| 5 | Ahmed Worker | ahmed.worker@nutrimap.com | worker123 | FIELD_WORKER | B001 |
| 6 | Fatima Worker | fatima.worker@nutrimap.com | worker123 | FIELD_WORKER | B002 |
| 7 | Karim Worker | karim.worker@nutrimap.com | worker123 | FIELD_WORKER | B002 |

### User Roles Permission Summary:
- **ADMIN**: Full access to all features (Users, Children, Visits, Dashboard with all stats)
- **SUPERVISOR**: Access to Children, Visits, Dashboard (Users read-only)
- **FIELD_WORKER**: Access to Children, Visits, Dashboard (no branch/area stats visible)

---

## 2. CHILDREN TEST DATA

| ID | Full Name | Father's Name | Mother's Name | Gender | Date of Birth | Contact | Division | District | Upazila | Union | Branch ID |
|----|-----------|---------------|---------------|--------|---------------|---------|----------|----------|---------|-------|-----------|
| 1 | Ayesha Rahman | Mohammad Rahman | Fatima Rahman | Female | 2023-05-15 | 01712345678 | Dhaka | Dhaka | Dhanmondi | Dhanmondi West | B001 |
| 2 | Imran Hossain | Kabir Hossain | Salma Hossain | Male | 2022-11-20 | 01812345678 | Dhaka | Dhaka | Gulshan | Gulshan South | B001 |
| 3 | Nadia Islam | Abdul Islam | Rehana Islam | Female | 2024-01-10 | 01912345678 | Chittagong | Chittagong | Pahartali | Pahartali North | B002 |
| 4 | Fahim Ahmed | Jamal Ahmed | Nasreen Ahmed | Male | 2023-08-25 | 01612345678 | Dhaka | Dhaka | Mirpur | Mirpur-10 | B001 |
| 5 | Sadia Begum | Rafiq Begum | Amina Begum | Female | 2022-06-05 | 01512345678 | Chittagong | Chittagong | Kotwali | Kotwali Ward-5 | B002 |
| 6 | Tanvir Hasan | Hasan Ali | Razia Hasan | Male | 2023-02-14 | 01412345678 | Sylhet | Sylhet | Sylhet Sadar | Amberkhana | B001 |
| 7 | Mim Akter | Rahim Akter | Julekha Akter | Female | 2024-03-01 | 01312345678 | Dhaka | Narayanganj | Siddhirganj | Siddhirganj South | B002 |
| 8 | Rafsan Khan | Iqbal Khan | Shirin Khan | Male | 2022-09-18 | 01712349876 | Rajshahi | Rajshahi | Rajpara | Rajpara Ward-3 | B001 |

### Age Reference (as of January 2026):
- Ayesha Rahman: ~20 months
- Imran Hossain: ~38 months (3y 2m)
- Nadia Islam: ~12 months
- Fahim Ahmed: ~29 months (2y 5m)
- Sadia Begum: ~43 months (3y 7m)
- Tanvir Hasan: ~35 months (2y 11m)
- Mim Akter: ~10 months
- Rafsan Khan: ~40 months (3y 4m)

---

## 3. VISITS TEST DATA

| ID | Child ID | Child Name | Visit Date | Weight (kg) | Height (cm) | MUAC (mm) | Risk Level | Notes | Entered By |
|----|----------|------------|------------|-------------|-------------|-----------|------------|-------|------------|
| 1 | 1 | Ayesha Rahman | 2025-11-15 | 10.5 | 78.0 | 135 | LOW | Initial assessment, healthy | 4 |
| 2 | 1 | Ayesha Rahman | 2025-12-15 | 10.8 | 79.5 | 138 | LOW | Good progress | 4 |
| 3 | 1 | Ayesha Rahman | 2026-01-10 | 11.0 | 80.2 | 140 | LOW | Excellent growth | 4 |
| 4 | 2 | Imran Hossain | 2025-10-20 | 12.0 | 88.0 | 125 | MEDIUM | Slightly underweight | 5 |
| 5 | 2 | Imran Hossain | 2025-11-22 | 12.5 | 89.0 | 128 | MEDIUM | Weight gain observed | 5 |
| 6 | 2 | Imran Hossain | 2025-12-20 | 12.8 | 90.0 | 132 | LOW | Improved nutrition | 5 |
| 7 | 3 | Nadia Islam | 2025-12-10 | 7.0 | 68.0 | 115 | HIGH | Severe malnutrition, needs intervention | 6 |
| 8 | 3 | Nadia Islam | 2026-01-05 | 7.5 | 69.5 | 120 | HIGH | Slight improvement, continue monitoring | 6 |
| 9 | 4 | Fahim Ahmed | 2025-11-25 | 11.5 | 85.0 | 140 | LOW | Healthy, normal growth | 4 |
| 10 | 4 | Fahim Ahmed | 2026-01-08 | 12.0 | 87.0 | 142 | LOW | Consistent growth | 4 |
| 11 | 5 | Sadia Begum | 2025-09-05 | 10.5 | 90.0 | 118 | HIGH | Started nutrition program | 7 |
| 12 | 5 | Sadia Begum | 2025-10-10 | 11.0 | 91.0 | 122 | MEDIUM | Responding to treatment | 7 |
| 13 | 5 | Sadia Begum | 2025-11-15 | 11.8 | 92.5 | 128 | MEDIUM | Good progress | 7 |
| 14 | 5 | Sadia Begum | 2025-12-20 | 12.5 | 94.0 | 135 | LOW | Recovered, continue monitoring | 7 |
| 15 | 6 | Tanvir Hasan | 2025-12-14 | 12.0 | 88.0 | 125 | MEDIUM | First visit, mild malnutrition | 5 |
| 16 | 6 | Tanvir Hasan | 2026-01-09 | 12.3 | 89.0 | 130 | LOW | Improving | 5 |
| 17 | 7 | Mim Akter | 2025-12-01 | 6.5 | 65.0 | 110 | HIGH | Severe acute malnutrition | 6 |
| 18 | 7 | Mim Akter | 2026-01-06 | 7.0 | 67.0 | 115 | HIGH | Enrolled in feeding program | 6 |
| 19 | 8 | Rafsan Khan | 2025-10-18 | 13.0 | 93.0 | 145 | LOW | Healthy child | 4 |
| 20 | 8 | Rafsan Khan | 2025-12-18 | 13.8 | 95.0 | 148 | LOW | Excellent growth | 4 |

---

## 4. RISK LEVEL DISTRIBUTION SUMMARY

| Risk Level | Count | Children |
|------------|-------|----------|
| LOW | 4 | Ayesha, Fahim, Sadia (recovered), Rafsan |
| MEDIUM | 1 | Tanvir (improving) |
| HIGH | 3 | Nadia, Mim, (Sadia was HIGH initially) |

---

## 5. LOGIN CREDENTIALS FOR TESTING

### Admin Access
- **Email**: admin@nutrimap.com
- **Password**: admin123

### Supervisor Access
- **Email**: john.super@nutrimap.com
- **Password**: super123

### Field Worker Access
- **Email**: maria.worker@nutrimap.com
- **Password**: worker123

---

## 6. EXPECTED TEST SCENARIOS

### A. Dashboard Tests
1. Login as Admin → Should see full dashboard with area summary table
2. Login as Supervisor → Should see dashboard with area summary
3. Login as Field Worker → Should see dashboard WITHOUT area summary

### B. User Management Tests
1. Login as Admin → Should be able to create/edit/delete users
2. Login as Supervisor → Should see users list (read-only)
3. Login as Field Worker → Should NOT see Users menu

### C. Children Tests
1. View children list with pagination
2. Filter children by risk level (HIGH, MEDIUM, LOW)
3. Create new child with all required fields
4. View child profile with growth chart
5. Verify age calculation is correct

### D. Visit Tests
1. View visits for a specific child
2. Create new visit with weight, height, MUAC
3. Verify risk level auto-calculation based on measurements
4. Verify growth chart updates with new visits

### E. Growth Chart Tests
1. View Ayesha Rahman → Should show 3 data points
2. View Sadia Begum → Should show 4 data points showing improvement
3. View Nadia Islam → Should show 2 data points at HIGH risk

---

## 7. MUAC RISK LEVEL REFERENCE

| MUAC Value | Risk Level | Color Code |
|------------|------------|------------|
| < 115 mm | HIGH (SAM) | Red |
| 115-124 mm | HIGH (MAM) | Orange |
| 125-134 mm | MEDIUM | Yellow |
| ≥ 135 mm | LOW | Green |

---

## 8. FIREBASE DOCUMENT IDs (for manual testing)

These can be used as reference when checking Firestore:

### User Document IDs
- admin@nutrimap.com → user_admin_001
- john.super@nutrimap.com → user_super_001
- maria.worker@nutrimap.com → user_worker_001

### Child Document IDs
- Ayesha Rahman → child_001
- Imran Hossain → child_002
- Nadia Islam → child_003
- Fahim Ahmed → child_004
- Sadia Begum → child_005
- Tanvir Hasan → child_006
- Mim Akter → child_007
- Rafsan Khan → child_008

### Visit Document IDs
- Format: visit_{childId}_{sequence}
- Example: visit_001_001, visit_001_002, visit_001_003 (for Ayesha's 3 visits)

---

*Generated on: January 12, 2026*
*For: NutriMap Android Application Testing*

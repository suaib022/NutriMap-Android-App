/**
 * Firebase Data Population Script
 * 
 * This script deletes existing children and visits data,
 * then creates 30 children and 70 visits with various combinations:
 * - Multiple branches (5 branches across different divisions)
 * - Both genders (Male/Female)
 * - Various age ranges (6 months to 5 years)
 * - All risk levels (Low, Medium, High)
 * - Various nutrition statuses (Normal, MAM, SAM)
 * - Different MUAC ranges
 */

const { initializeApp } = require('firebase/app');
const { 
    getFirestore, 
    collection, 
    getDocs, 
    addDoc, 
    deleteDoc,
    doc,
    writeBatch
} = require('firebase/firestore');

// Firebase configuration from google-services.json
const firebaseConfig = {
    apiKey: "AIzaSyDpnKC8fKmtwyWfP_EOxR_Wji4aqGZsu6c",
    projectId: "nutrimap-e1e2a",
    storageBucket: "nutrimap-e1e2a.firebasestorage.app"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// Helper function to generate random date of birth for children (6 months to 5 years old)
function generateDateOfBirth(ageInMonths) {
    const today = new Date();
    const dob = new Date(today);
    dob.setMonth(dob.getMonth() - ageInMonths);
    return dob.toISOString().split('T')[0]; // Format: YYYY-MM-DD
}

// Helper function to generate visit date
function generateVisitDate(monthsAgo) {
    const today = new Date();
    const visitDate = new Date(today);
    visitDate.setMonth(visitDate.getMonth() - monthsAgo);
    return visitDate.toISOString().split('T')[0];
}

// Branch data covering multiple divisions
const branches = [
    { id: "1", name: "Comilla Main Branch", division: "Chattogram", divisionId: "1", district: "Comilla", districtId: "1", upazila: "Debidwar", upazilaId: "1", union: "Moynamoti", unionId: "1" },
    { id: "2", name: "Debidwar Branch", division: "Chattogram", divisionId: "1", district: "Comilla", districtId: "1", upazila: "Burichang", upazilaId: "3", union: "Bakshimul", unionId: "31" },
    { id: "100", name: "Dhaka Central", division: "Dhaka", divisionId: "6", district: "Dhaka", districtId: "47", upazila: "Dhamrai", upazilaId: "339", union: "Nannar", unionId: "2900" },
    { id: "50", name: "Rajshahi Branch", division: "Rajshahi", divisionId: "2", district: "Bogra", districtId: "12", upazila: "Sariakandi", upazilaId: "103", union: "Hatfulbari", unionId: "800" },
    { id: "75", name: "Sylhet Branch", division: "Sylhet", divisionId: "4", district: "Sylhet", districtId: "36", upazila: "Bishwanath", upazilaId: "278", union: "Deokalos", unionId: "2200" }
];

// Name pools
const maleFirstNames = ["Arif", "Rifat", "Imran", "Tanvir", "Rasel", "Mehedi", "Sakib", "Naim", "Fahad", "Hasan", "Jubair", "Rakib", "Shanto", "Faisal", "Omar"];
const femaleFirstNames = ["Fatima", "Nadia", "Sadia", "Ayesha", "Mitu", "Shila", "Nusrat", "Sharmin", "Jannatul", "Tahmina", "Riya", "Sumi", "Afrin", "Lamia", "Tasnim"];
const lastNames = ["Rahman", "Akter", "Hasan", "Islam", "Khan", "Begum", "Khatun", "Ahmed", "Mia", "Rani", "Hossain", "Chowdhury", "Uddin", "Sultana", "Karim"];
const fatherFirstNames = ["Kamal", "Jobbar", "Belal", "Raju", "Aziz", "Hanif", "Rafiq", "Sohel", "Jashim", "Mokhles", "Abdul", "Md.", "Jamal", "Nazrul", "Shafiq"];
const motherFirstNames = ["Fatima", "Hasina", "Sumaya", "Roksana", "Rokeya", "Nasima", "Kulsum", "Amena", "Bilkis", "Josna", "Rahima", "Selina", "Halima", "Salma", "Parveen"];

// Children data - 30 children
const childrenData = [];

for (let i = 0; i < 30; i++) {
    const isMale = i % 2 === 0;
    const branch = branches[i % branches.length];
    const ageInMonths = 6 + Math.floor(Math.random() * 54); // 6 months to 5 years
    
    const firstName = isMale 
        ? maleFirstNames[i % maleFirstNames.length] 
        : femaleFirstNames[i % femaleFirstNames.length];
    const lastName = lastNames[i % lastNames.length];
    const fatherFirst = fatherFirstNames[i % fatherFirstNames.length];
    const motherFirst = motherFirstNames[i % motherFirstNames.length];
    
    childrenData.push({
        name: `${firstName} ${lastName}`,
        fatherName: `${fatherFirst} ${lastName}`,
        motherName: `${motherFirst} ${lastName}`,
        contact: `017${10000000 + i}`,
        gender: isMale ? "Male" : "Female",
        dateOfBirth: generateDateOfBirth(ageInMonths),
        branchId: branch.id,
        branchName: branch.name,
        division: branch.division,
        divisionId: branch.divisionId,
        district: branch.district,
        districtId: branch.districtId,
        upazilla: branch.upazila,
        upazilaId: branch.upazilaId,
        unionName: branch.union,
        unionId: branch.unionId,
        lastVisit: ""
    });
}

// Visit variations for different health statuses
// MUAC: <115mm = SAM (High), 115-124mm = MAM (Medium), >=125mm = Normal (Low)
const visitProfiles = [
    // Low risk - Healthy children
    { muacMin: 130, muacMax: 150, weightFactor: 1.0, heightFactor: 1.0, riskLevel: "Low", notes: ["Healthy checkup", "Growing well", "Normal development", "Good nutrition"] },
    { muacMin: 125, muacMax: 135, weightFactor: 0.95, heightFactor: 0.98, riskLevel: "Low", notes: ["Stable condition", "Regular checkup", "Adequate nutrition", "No concerns"] },
    
    // Medium risk - MAM cases
    { muacMin: 115, muacMax: 124, weightFactor: 0.85, heightFactor: 0.95, riskLevel: "Medium", notes: ["MAM detected", "Moderate concern", "Under observation", "Nutritional support needed"] },
    { muacMin: 118, muacMax: 124, weightFactor: 0.88, heightFactor: 0.96, riskLevel: "Medium", notes: ["Slight underweight", "Follow-up required", "MAM noted", "Diet improvement needed"] },
    
    // High risk - SAM cases
    { muacMin: 105, muacMax: 114, weightFactor: 0.75, heightFactor: 0.92, riskLevel: "High", notes: ["SAM detected", "Severe case", "Urgent treatment required", "Critical condition"] },
    { muacMin: 100, muacMax: 112, weightFactor: 0.70, heightFactor: 0.90, riskLevel: "High", notes: ["Severe malnutrition", "Hospital referral needed", "Emergency case", "Immediate intervention"] }
];

// Function to calculate expected weight for age (simplified WHO standards approximation)
function getExpectedWeight(ageInMonths, gender) {
    // Simplified approximation
    if (ageInMonths <= 6) return gender === "Male" ? 7.0 : 6.5;
    if (ageInMonths <= 12) return gender === "Male" ? 9.0 : 8.5;
    if (ageInMonths <= 24) return gender === "Male" ? 11.5 : 11.0;
    if (ageInMonths <= 36) return gender === "Male" ? 13.5 : 13.0;
    if (ageInMonths <= 48) return gender === "Male" ? 15.5 : 15.0;
    return gender === "Male" ? 17.5 : 17.0;
}

// Function to calculate expected height for age (simplified)
function getExpectedHeight(ageInMonths, gender) {
    if (ageInMonths <= 6) return gender === "Male" ? 65.0 : 63.0;
    if (ageInMonths <= 12) return gender === "Male" ? 75.0 : 73.0;
    if (ageInMonths <= 24) return gender === "Male" ? 85.0 : 83.0;
    if (ageInMonths <= 36) return gender === "Male" ? 95.0 : 93.0;
    if (ageInMonths <= 48) return gender === "Male" ? 102.0 : 100.0;
    return gender === "Male" ? 108.0 : 106.0;
}

// Generate visits - at least one per child, total 70 visits
function generateVisitsForChildren(childDocIds) {
    const visits = [];
    let visitCount = 0;
    const targetVisits = 70;
    
    // First pass: ensure each child has at least one visit
    childDocIds.forEach((docId, index) => {
        const child = childrenData[index];
        const dob = new Date(child.dateOfBirth);
        const today = new Date();
        const ageInMonths = Math.floor((today - dob) / (1000 * 60 * 60 * 24 * 30));
        
        // Assign health status based on index for variety
        const profileIndex = index % visitProfiles.length;
        const profile = visitProfiles[profileIndex];
        
        const expectedWeight = getExpectedWeight(ageInMonths, child.gender);
        const expectedHeight = getExpectedHeight(ageInMonths, child.gender);
        
        const muac = profile.muacMin + Math.floor(Math.random() * (profile.muacMax - profile.muacMin));
        const weight = Math.round((expectedWeight * profile.weightFactor + (Math.random() * 1 - 0.5)) * 10) / 10;
        const height = Math.round((expectedHeight * profile.heightFactor + (Math.random() * 2 - 1)) * 10) / 10;
        
        visits.push({
            childDocumentId: docId,
            childId: 0,
            visitDate: generateVisitDate(Math.floor(Math.random() * 3)), // Within last 3 months
            weightKg: weight,
            heightCm: height,
            muacMm: muac,
            riskLevel: profile.riskLevel,
            notes: profile.notes[Math.floor(Math.random() * profile.notes.length)],
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            enteredBy: 1,
            deleted: false
        });
        visitCount++;
    });
    
    // Second pass: add additional visits to reach 70 total
    let childIndex = 0;
    while (visitCount < targetVisits) {
        const docId = childDocIds[childIndex % childDocIds.length];
        const child = childrenData[childIndex % childrenData.length];
        const dob = new Date(child.dateOfBirth);
        const today = new Date();
        const ageInMonths = Math.floor((today - dob) / (1000 * 60 * 60 * 24 * 30));
        
        // Vary the profile for multiple visits on the same child (showing improvement or deterioration)
        const profileIndex = (childIndex + visitCount) % visitProfiles.length;
        const profile = visitProfiles[profileIndex];
        
        const expectedWeight = getExpectedWeight(ageInMonths, child.gender);
        const expectedHeight = getExpectedHeight(ageInMonths, child.gender);
        
        const muac = profile.muacMin + Math.floor(Math.random() * (profile.muacMax - profile.muacMin));
        const weight = Math.round((expectedWeight * profile.weightFactor + (Math.random() * 1 - 0.5)) * 10) / 10;
        const height = Math.round((expectedHeight * profile.heightFactor + (Math.random() * 2 - 1)) * 10) / 10;
        
        // Visits spread across different time periods
        const monthsAgo = 1 + Math.floor(visitCount / 10) + Math.floor(Math.random() * 3);
        
        visits.push({
            childDocumentId: docId,
            childId: 0,
            visitDate: generateVisitDate(monthsAgo),
            weightKg: weight,
            heightCm: height,
            muacMm: muac,
            riskLevel: profile.riskLevel,
            notes: profile.notes[Math.floor(Math.random() * profile.notes.length)],
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            enteredBy: 1,
            deleted: false
        });
        
        visitCount++;
        childIndex++;
    }
    
    return visits;
}

// Delete all documents in a collection
async function deleteCollection(collectionName) {
    console.log(`Deleting all documents from ${collectionName}...`);
    const querySnapshot = await getDocs(collection(db, collectionName));
    
    let deleteCount = 0;
    for (const docSnapshot of querySnapshot.docs) {
        await deleteDoc(doc(db, collectionName, docSnapshot.id));
        deleteCount++;
    }
    console.log(`Deleted ${deleteCount} documents from ${collectionName}`);
}

// Main function
async function main() {
    try {
        console.log("=== NutriMap Firebase Data Population Script ===\n");
        
        // Step 1: Delete existing children and visits
        console.log("Step 1: Deleting existing data...");
        await deleteCollection("children");
        await deleteCollection("visits");
        console.log("Existing data deleted.\n");
        
        // Step 2: Add children
        console.log("Step 2: Adding 30 children...");
        const childDocIds = [];
        const childrenCollection = collection(db, "children");
        
        for (const child of childrenData) {
            const docRef = await addDoc(childrenCollection, child);
            childDocIds.push(docRef.id);
            console.log(`  Added child: ${child.name} (${docRef.id})`);
        }
        console.log(`Added ${childDocIds.length} children.\n`);
        
        // Step 3: Generate and add visits
        console.log("Step 3: Generating 70 visits...");
        const visits = generateVisitsForChildren(childDocIds);
        const visitsCollection = collection(db, "visits");
        
        let visitNumber = 0;
        for (const visit of visits) {
            const childIndex = childDocIds.indexOf(visit.childDocumentId);
            const childName = childrenData[childIndex].name;
            await addDoc(visitsCollection, visit);
            visitNumber++;
            console.log(`  Added visit ${visitNumber}: ${childName} - ${visit.riskLevel} risk, MUAC: ${visit.muacMm}mm`);
        }
        console.log(`Added ${visitNumber} visits.\n`);
        
        // Summary
        console.log("=== Summary ===");
        console.log(`Total children added: ${childDocIds.length}`);
        console.log(`Total visits added: ${visitNumber}`);
        console.log("\nBranch distribution:");
        branches.forEach(branch => {
            const count = childrenData.filter(c => c.branchId === branch.id).length;
            console.log(`  ${branch.name}: ${count} children`);
        });
        
        console.log("\nRisk level distribution in visits:");
        const lowRisk = visits.filter(v => v.riskLevel === "Low").length;
        const mediumRisk = visits.filter(v => v.riskLevel === "Medium").length;
        const highRisk = visits.filter(v => v.riskLevel === "High").length;
        console.log(`  Low: ${lowRisk}`);
        console.log(`  Medium: ${mediumRisk}`);
        console.log(`  High: ${highRisk}`);
        
        console.log("\nGender distribution:");
        const males = childrenData.filter(c => c.gender === "Male").length;
        const females = childrenData.filter(c => c.gender === "Female").length;
        console.log(`  Male: ${males}`);
        console.log(`  Female: ${females}`);
        
        console.log("\n=== Data population complete! ===");
        process.exit(0);
        
    } catch (error) {
        console.error("Error:", error);
        process.exit(1);
    }
}

main();

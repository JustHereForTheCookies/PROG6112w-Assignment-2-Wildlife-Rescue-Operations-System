package wildlife;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/*
 * WildLife SA Rescue
 * Name: Neo Praise Mahlangu
 * Student No: ST10521004@rcconnect.
 *
 * AI-use disclosure: ChatGPT was consulted for planning and debugging.
 */

// ================= Rescuable =================

/** Operations every rescue e must support. */
interface Rescuable {
    void startRescue();
    void completeRescue();
    String generateSummary();
}

// ================= RescueStatus =================

enum RescueStatus {
    REPORTED("Reported"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    RescueStatus(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}

// ================= RescuePriority =================

enum RescuePriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

// ================= RescueCase =================

/** base class holding the data common to all rescue types. */
abstract class RescueCase implements Rescuable {

    private final String caseId;
    private final String animalName;
    private final String species;
    private final String location;
    private final String ranger;
    private final int rescueDays;
    private final double dailyCareCost;
    private RescueStatus status;

    protected RescueCase(String caseId, String animalName, String species, String location,
                String ranger, int rescueDays, double dailyCareCost) {
        requireText(caseId, "Rescue Case ID");
        requireText(species, "Species");
        requireText(location, "Rescue Location");
        requireText(ranger, "Assigned Ranger");
        requirePositive(rescueDays, "Number of Rescue Days");
        requirePositive(dailyCareCost, "Daily Care Cost");
        this.caseId = caseId.trim();
        this.animalName = (animalName == null || animalName.isBlank()) ? "Unnamed" : animalName.trim();
        this.species = species.trim();
        this.location = location.trim();
        this.ranger = ranger.trim();
        this.rescueDays = rescueDays;
        this.dailyCareCost = dailyCareCost;
        this.status = RescueStatus.REPORTED;
    }

    // ---- validation helpers (alsom subclasses) ----
    protected static void requireText(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " must not be blank.");
    }

    protected static void requirePositive(double value, String field) {
        if (value <= 0)
            throw new IllegalArgumentException(field + " must be greater than zero.");
    }

    // ---- each rescue type supplies its own ----
    public abstract String getRescueType();
    protected abstract double calculateAdditionalCost();
    public abstract RescuePriority determinePriority();
    public abstract String getSpecificDetails();

    /**care cost plus the type-specific costs. */
    public double calculateTotalCost() {
        return rescueDays * dailyCareCost + calculateAdditionalCost();
    }

    // ---- Rescuable ----
    @Override
    public void startRescue() {
        if (status != RescueStatus.REPORTED)
            throw new IllegalStateException("Only a reported rescue can be started (current status: " + status + ").");
        status = RescueStatus.IN_PROGRESS;
    }

    @Override
    public void completeRescue() {
        if (status != RescueStatus.IN_PROGRESS)
            throw new IllegalStateException("Only an in-progress rescue can be completed (current status: " + status + ").");
        status = RescueStatus.COMPLETED;
    }

    @Override
    public String generateSummary() {
        return String.format(
            "--- Rescue Summary ---%n" +
            "Rescue Case ID : %s%n" +
            "Rescue Type    : %s%n" +
            "Species        : %s%n" +
            "Assigned Ranger: %s%n" +
            "Priority       : %s%n" +
            "Status         : %s%n" +
            "Total Cost     : %s",
            caseId, getRescueType(), species, ranger, determinePriority(), status,
            formatMoney(calculateTotalCost()));
    }

    public static String formatMoney(double amount) {
        return String.format(java.util.Locale.US, "R%,.2f", amount);
    }

    // ---- getters controlled setter ----
    public String getCaseId() { return caseId; }
    public String getAnimalName() { return animalName; }
    public String getSpecies() { return species; }
    public String getLocation() { return location; }
    public String getRanger() { return ranger; }
    public int getRescueDays() { return rescueDays; }
    public double getDailyCareCost() { return dailyCareCost; }
    public RescueStatus getStatus() { return status; }

    public void setStatus(RescueStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status must not be null.");
        this.status = newStatus;
    }

    @Override
    public String toString() {
        return String.format("ID: %s | %s | Animal: %s | Species: %s | Location: %s | Ranger: %s%n" +
                "      Days: %d | Daily Cost: %s | Status: %s | Priority: %s | Total: %s%n      %s",
                caseId, getRescueType(), animalName, species, location, ranger,
                rescueDays, formatMoney(dailyCareCost), status, determinePriority(),
                formatMoney(calculateTotalCost()), getSpecificDetails());
    }
}

// ================= InjuredAnimalRescue =================

class InjuredAnimalRescue extends RescueCase {
    public static final double SURGERY_FEE = 5000.0;
    public static final double HIGH_VET_COST_THRESHOLD = 10000.0;

    private final String injuryDescription;
    private final double veterinaryCost;
    private final boolean surgeryRequired;

    public InjuredAnimalRescue(String caseId, String animalName, String species, String location,
                               String ranger, int days, double dailyCost,
                               String injuryDescription, double veterinaryCost, boolean surgeryRequired) {
        super(caseId, animalName, species, location, ranger, days, dailyCost);
        requireText(injuryDescription, "Injury Description");
        requirePositive(veterinaryCost, "Veterinary Treatment Cost");
        this.injuryDescription = injuryDescription.trim();
        this.veterinaryCost = veterinaryCost;
        this.surgeryRequired = surgeryRequired;
    }

    @Override public String getRescueType() { return "Injured Animal"; }

    @Override
    protected double calculateAdditionalCost() {
        return veterinaryCost + (surgeryRequired ? SURGERY_FEE : 0);
    }

    /** Surgery = CRITICAL; expensive treatment = HIGH; otherwise MEDIUM. */
    @Override
    public RescuePriority determinePriority() {
        if (surgeryRequired) return RescuePriority.CRITICAL;
        if (veterinaryCost >= HIGH_VET_COST_THRESHOLD) return RescuePriority.HIGH;
        return RescuePriority.MEDIUM;
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Injury: %s | Vet Cost: %s | Surgery Required: %s",
                injuryDescription, formatMoney(veterinaryCost), surgeryRequired ? "Yes" : "No");
    }

    public String getInjuryDescription() { return injuryDescription; }
    public double getVeterinaryCost() { return veterinaryCost; }
    public boolean isSurgeryRequired() { return surgeryRequired; }
}

// ================= OrphanedAnimalRescue =================

class OrphanedAnimalRescue extends RescueCase {
    public static final double FOSTER_CARE_FEE = 2500.0;

    private final int ageInMonths;
    private final double feedingCost;
    private final boolean fosterCareRequired;

    public OrphanedAnimalRescue(String caseId, String animalName, String species, String location,
                                String ranger, int days, double dailyCost,
                                int ageInMonths, double feedingCost, boolean fosterCareRequired) {
        super(caseId, animalName, species, location, ranger, days, dailyCost);
        requirePositive(ageInMonths, "Estimated Age (Months)");
        requirePositive(feedingCost, "Feeding Cost");
        this.ageInMonths = ageInMonths;
        this.feedingCost = feedingCost;
        this.fosterCareRequired = fosterCareRequired;
    }

    @Override public String getRescueType() { return "Orphaned Animal"; }

    @Override
    protected double calculateAdditionalCost() {
        return feedingCost + (fosterCareRequired ? FOSTER_CARE_FEE : 0);
    }

    /** Younger animals are more vulnerable: 
     * <=3 months HIGH, <=12 MEDIUM, older LOW. */
    @Override
    public RescuePriority determinePriority() {
        if (ageInMonths <= 3) return RescuePriority.HIGH;
        if (ageInMonths <= 12) return RescuePriority.MEDIUM;
        return RescuePriority.LOW;
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Age: %d months | Feeding Cost: %s | Foster Care Required: %s",
                ageInMonths, formatMoney(feedingCost), fosterCareRequired ? "Yes" : "No");
    }

    public int getAgeInMonths() { return ageInMonths; }
    public double getFeedingCost() { return feedingCost; }
    public boolean isFosterCareRequired() { return fosterCareRequired; }
}

// ================= EndangeredSpeciesRescue =================

class EndangeredSpeciesRescue extends RescueCase {
    public static final double SPECIALIST_TEAM_FEE = 8000.0;

    private final String classification;
    private final double securityCost;
    private final boolean specialistTeamRequired;

    public EndangeredSpeciesRescue(String caseId, String animalName, String species, String location,
                                   String ranger, int days, double dailyCost,
                                   String classification, double securityCost, boolean specialistTeamRequired) {
        super(caseId, animalName, species, location, ranger, days, dailyCost);
        requireText(classification, "Conservation Classification");
        requirePositive(securityCost, "Security Cost");
        this.classification = classification.trim();
        this.securityCost = securityCost;
        this.specialistTeamRequired = specialistTeamRequired;
    }

    @Override public String getRescueType() { return "Endangered Species"; }

    @Override
    protected double calculateAdditionalCost() {
        return securityCost + (specialistTeamRequired ? SPECIALIST_TEAM_FEE : 0);
    }

    /** Critically Endangered or specialist team = CRITICAL; all other endangered species = HIGH. */
    @Override
    public RescuePriority determinePriority() {
        if (specialistTeamRequired || classification.equalsIgnoreCase("Critically Endangered"))
            return RescuePriority.CRITICAL;
        return RescuePriority.HIGH;
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Classification: %s | Security Cost: %s | Specialist Team Required: %s",
                classification, formatMoney(securityCost), specialistTeamRequired ? "Yes" : "No");
    }

    public String getClassification() { return classification; }
    public double getSecurityCost() { return securityCost; }
    public boolean isSpecialistTeamRequired() { return specialistTeamRequired; }
}

// ================= RescueManager =================

/** Manages the collection of rescue cases (ArrayList). */
class RescueManager {
    private final ArrayList<RescueCase> cases = new ArrayList<>();

    /** @throws IllegalArgumentException if the case is null or its ID already exists */
    public void addCase(RescueCase rescueCase) {
        if (rescueCase == null) throw new IllegalArgumentException("Rescue case must not be null.");
        if (idExists(rescueCase.getCaseId()))
            throw new IllegalArgumentException("Rescue Case ID '" + rescueCase.getCaseId() + "' already exists.");
        cases.add(rescueCase);
    }

    public boolean idExists(String id) {
        return findById(id) != null;
    }

    /** @return the matching case (ID is case-insensitive) but null if not found */
    public RescueCase findById(String id) {
        if (id == null) return null;
        for (RescueCase c : cases) {
            if (c.getCaseId().equalsIgnoreCase(id.trim())) return c;
        }
        return null;
    }

    public boolean updateStatus(String id, RescueStatus newStatus) {
        RescueCase c = findById(id);
        if (c == null) return false;
        c.setStatus(newStatus);
        return true;
    }

    public List<RescueCase> getAllCases() {
        return Collections.unmodifiableList(cases);
    }

    public int getCaseCount() { return cases.size(); }

    public double getTotalEstimatedCost() {
        double total = 0;
        for (RescueCase c : cases) total += c.calculateTotalCost(); // polymorphic call
        return total;
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(128);
        sb.append(line).append(System.lineSeparator());
        sb.append("                                     WILDLIFE SA - RESCUE REPORT").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("%-8s %-20s %-16s %-18s %-16s %-10s %-12s %14s%n",
                "ID", "Type", "Species", "Location", "Ranger", "Priority", "Status", "Total Cost"));
        sb.append("-".repeat(128)).append(System.lineSeparator());
        if (cases.isEmpty()) sb.append("No rescue cases recorded.").append(System.lineSeparator());
        for (RescueCase c : cases) {
            sb.append(String.format("%-8s %-20s %-16s %-18s %-16s %-10s %-12s %14s%n",
                    c.getCaseId(), c.getRescueType(), c.getSpecies(), c.getLocation(), c.getRanger(),
                    c.determinePriority(), c.getStatus(), RescueCase.formatMoney(c.calculateTotalCost())));
        }
        sb.append("-".repeat(128)).append(System.lineSeparator());
        sb.append(String.format("Total Rescue Cases        : %d%n", getCaseCount()));
        sb.append(String.format("Total Estimated Rescue Cost: %s%n", RescueCase.formatMoney(getTotalEstimatedCost())));
        sb.append(line);
        return sb.toString();
    }
}

// ================= Main =================

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final RescueManager manager = new RescueManager();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            switch (readInt("Enter choice: ", 1, 9)) {
                case 1 -> createCase();
                case 2 -> searchCase();
                case 3 -> updateStatus();
                case 4 -> displayAll();
                case 5 -> operate("start");
                case 6 -> operate("complete");
                case 7 -> operate("summary");
                case 8 -> System.out.println(manager.generateReport());
                case 9 -> { running = false; System.out.println("Goodbye."); }
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== WildLife SA Rescue Operations =====");
        System.out.println("1. Create new rescue case");
        System.out.println("2. Search rescue case by ID");
        System.out.println("3. Update rescue status");
        System.out.println("4. Display all rescue cases");
        System.out.println("5. Start rescue operation");
        System.out.println("6. Complete rescue operation");
        System.out.println("7. Generate rescue summary");
        System.out.println("8. Generate rescue report");
        System.out.println("9. Exit");
    }

    private static void createCase() {
        System.out.println("\nRescue type: 1. Injured  2. Orphaned  3. Endangered Species");
        int type = readInt("Select type: ", 1, 3);

        String id;
        while (true) {
            id = readText("Rescue Case ID: ");
            if (manager.idExists(id)) System.out.println("Error: that Rescue Case ID already exists.");
            else break;
        }
        System.out.print("Animal Name (optional): ");
        String name = in.nextLine();
        String species = readText("Species: ");
        String location = readText("Rescue Location: ");
        String ranger = readText("Assigned Ranger: ");
        int days = readInt("Number of Rescue Days: ", 1, Integer.MAX_VALUE);
        double daily = readPositiveDouble("Daily Care Cost (R): ");

        RescueCase rc;
        switch (type) {
            case 1 -> rc = new InjuredAnimalRescue(id, name, species, location, ranger, days, daily,
                    readText("Injury Description: "),
                    readPositiveDouble("Veterinary Treatment Cost (R): "),
                    readYesNo("Surgery required?"));
            case 2 -> rc = new OrphanedAnimalRescue(id, name, species, location, ranger, days, daily,
                    readInt("Estimated Age (months): ", 1, Integer.MAX_VALUE),
                    readPositiveDouble("Feeding Cost (R): "),
                    readYesNo("Foster care required?"));
            default -> rc = new EndangeredSpeciesRescue(id, name, species, location, ranger, days, daily,
                    readText("Conservation Classification: "),
                    readPositiveDouble("Security Cost (R): "),
                    readYesNo("Specialist team required?"));
        }
        manager.addCase(rc);
        System.out.println("Rescue case created successfully.");
    }

    private static void searchCase() {
        RescueCase c = manager.findById(readText("Rescue Case ID to search: "));
        System.out.println(c == null ? "No rescue case found with that ID." : c);
    }

    private static void updateStatus() {
        RescueCase c = manager.findById(readText("Rescue Case ID: "));
        if (c == null) { System.out.println("No rescue case found with that ID."); return; }
        System.out.println("Current status: " + c.getStatus());
        System.out.println("1. Reported  2. In Progress  3. Completed");
        int choice = readInt("New status: ", 1, 3);
        c.setStatus(RescueStatus.values()[choice - 1]);
        System.out.println("Status updated to: " + c.getStatus());
    }

    private static void displayAll() {
        if (manager.getCaseCount() == 0) { System.out.println("No rescue cases recorded."); return; }
        for (RescueCase c : manager.getAllCases()) System.out.println(c + System.lineSeparator());
    }

    private static void operate(String action) {
        RescueCase c = manager.findById(readText("Rescue Case ID: "));
        if (c == null) { System.out.println("No rescue case found with that ID."); return; }
        try {
            switch (action) {
                case "start" -> { c.startRescue(); System.out.println("Rescue started. Status: " + c.getStatus()); }
                case "complete" -> { c.completeRescue(); System.out.println("Rescue completed. Status: " + c.getStatus()); }
                default -> System.out.println(c.generateSummary());
            }
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- input helpers: keep prompting until the input is valid ----------
    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Error: this field cannot be blank.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(in.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println(max == Integer.MAX_VALUE
                    ? "Error: enter a whole number of at least " + min + "."
                    : "Error: enter a number between " + min + " and " + max + ".");
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(in.nextLine().trim());
                if (v > 0 && !Double.isInfinite(v)) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Error: enter a number greater than zero.");
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String s = in.nextLine().trim();
            if (s.equalsIgnoreCase("Y")) return true;
            if (s.equalsIgnoreCase("N")) return false;
            System.out.println("Error: enter Y or N.");
        }
    }
}
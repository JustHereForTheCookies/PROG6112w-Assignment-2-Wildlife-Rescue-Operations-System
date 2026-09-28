package wildlife;

//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class RescueManagerTest {

    @Test
    public void testInjuredTotalCostWithSurgery() {
        InjuredAnimalRescue r = new InjuredAnimalRescue(
            "WR101", "Elephant", "African Elephant", "Kruger",
            "Ranger A", 2, 1000, "Broken leg", 5000, true
        );
        assertEquals(2 * 1000 + 5000 + 5000, r.calculateTotalCost(), 0.001);
    }

    @Test
    public void testInjuredTotalCostWithoutSurgery() {
        InjuredAnimalRescue r = new InjuredAnimalRescue(
            "WR102", "Lion", "Lion", "Kruger",
            "Ranger B", 3, 1500, "Wound", 2000, false
        );
        assertEquals(3 * 1500 + 2000, r.calculateTotalCost(), 0.001);
    }

    @Test
    public void testOrphanedTotalCostWithFosterCare() {
        OrphanedAnimalRescue r = new OrphanedAnimalRescue(
            "WR103", "Rhino", "White Rhino", "Hluhluwe",
            "Ranger C", 4, 2000, 2, 1000, true
        );
        assertEquals(4 * 2000 + 1000 + 2500, r.calculateTotalCost(), 0.001);
    }

    @Test
    public void testOrphanedTotalCostWithoutFosterCare() {
        OrphanedAnimalRescue r = new OrphanedAnimalRescue(
            "WR104", "Buffalo", "Buffalo", "Kruger",
            "Ranger D", 5, 1000, 10, 500, false
        );
        assertEquals(5 * 1000 + 500, r.calculateTotalCost(), 0.001);
    }

    @Test
    public void testEndangeredTotalCostWithSpecialistTeam() {
        EndangeredSpeciesRescue r = new EndangeredSpeciesRescue(
            "WR105", "Pangolin", "Ground Pangolin", "Limpopo",
            "Ranger E", 3, 1500, "Critically Endangered", 3000, true
        );
        assertEquals(3 * 1500 + 3000 + 8000, r.calculateTotalCost(), 0.001);
    }

    @Test
    public void testEndangeredTotalCostWithoutSpecialistTeam() {
        EndangeredSpeciesRescue r = new EndangeredSpeciesRescue(
            "WR106", "Wild Dog", "African Wild Dog", "Kruger",
            "Ranger F", 2, 1200, "Endangered", 2000, false
        );
        assertEquals(2 * 1200 + 2000, r.calculateTotalCost(), 0.001);
    }

    @Test
    public void testPriorityCalculations() {
        InjuredAnimalRescue injuredCritical = new InjuredAnimalRescue(
            "WR107", "Lion", "Lion", "Kruger",
            "Ranger G", 1, 1000, "Wound", 2000, true
        );
        assertEquals(RescuePriority.CRITICAL, injuredCritical.determinePriority());

        InjuredAnimalRescue injuredHigh = new InjuredAnimalRescue(
            "WR108", "Leopard", "Leopard", "Kruger",
            "Ranger H", 1, 1000, "Wound", 12000, false
        );
        assertEquals(RescuePriority.HIGH, injuredHigh.determinePriority());

        OrphanedAnimalRescue orphanHigh = new OrphanedAnimalRescue(
            "WR109", "Rhino", "White Rhino", "Hluhluwe",
            "Ranger I", 1, 1000, 2, 500, false
        );
        assertEquals(RescuePriority.HIGH, orphanHigh.determinePriority());

        EndangeredSpeciesRescue endangeredCritical = new EndangeredSpeciesRescue(
            "WR110", "Pangolin", "Ground Pangolin", "Limpopo",
            "Ranger J", 1, 1000, "Critically Endangered", 1000, false
        );
        assertEquals(RescuePriority.CRITICAL, endangeredCritical.determinePriority());
    }

    @Test
    public void testStatusUpdates() {
        InjuredAnimalRescue r = new InjuredAnimalRescue(
            "WR111", "Leopard", "Leopard", "Kruger",
            "Ranger K", 1, 1000, "Injury", 1000, false
        );
        assertEquals(RescueStatus.REPORTED, r.getStatus());

        r.startRescue();
        assertEquals(RescueStatus.IN_PROGRESS, r.getStatus());

        r.completeRescue();
        assertEquals(RescueStatus.COMPLETED, r.getStatus());

        assertThrows(IllegalStateException.class, r::startRescue);
    }

    @Test
    public void testInvalidCompleteBeforeStart() {
        InjuredAnimalRescue r = new InjuredAnimalRescue(
            "WR112", "Buffalo", "Buffalo", "Kruger",
            "Ranger L", 1, 1000, "Injury", 1000, false
        );
        assertThrows(IllegalStateException.class, r::completeRescue);
    }

    @Test
    public void testSearchExistingCase() {
        RescueManager manager = new RescueManager();
        InjuredAnimalRescue r = new InjuredAnimalRescue(
            "WR113", "Buffalo", "Buffalo", "Kruger",
            "Ranger M", 2, 1000, "Injury", 1000, false
        );
        manager.addCase(r);
        assertNotNull(manager.findById("WR113"));
        assertEquals(r, manager.findById("WR113"));
    }

    @Test
    public void testSearchNonExistingCase() {
        RescueManager manager = new RescueManager();
        assertNull(manager.findById("WR999"));
    }

    //@Test
    //public void testDuplicateIdPrevented() {
    @Test(expected = IllegalArgumentException.class)
public void testDuplicateIdPrevented() {
        RescueManager manager = new RescueManager();
        InjuredAnimalRescue r1 = new InjuredAnimalRescue(
            "WR114", "Zebra", "Zebra", "Kruger",
            "Ranger N", 1, 1000, "Injury", 1000, false
        );
        InjuredAnimalRescue r2 = new InjuredAnimalRescue(
            "WR114", "Zebra", "Zebra", "Kruger",
            "Ranger O", 1, 1000, "Injury", 1000, false
        );
        manager.addCase(r1);
        //assertThrows(IllegalArgumentException.class, () -> manager.addCase(r2));
  
    }

    @Test
    public void testUpdateStatusThroughManager() {
        RescueManager manager = new RescueManager();
        InjuredAnimalRescue r = new InjuredAnimalRescue(
            "WR115", "Zebra", "Zebra", "Kruger",
            "Ranger P", 1, 1000, "Injury", 1000, false
        );
        manager.addCase(r);

        assertTrue(manager.updateStatus("WR115", RescueStatus.IN_PROGRESS));
        assertEquals(RescueStatus.IN_PROGRESS, manager.findById("WR115").getStatus());

        assertFalse(manager.updateStatus("WR999", RescueStatus.COMPLETED));
    }

    @Test
    public void testTotalEstimatedCost() {
        RescueManager manager = new RescueManager();
        InjuredAnimalRescue r1 = new InjuredAnimalRescue(
            "WR116", "Lion", "Lion", "Kruger",
            "Ranger Q", 2, 1000, "Injury", 2000, false
        );
        OrphanedAnimalRescue r2 = new OrphanedAnimalRescue(
            "WR117", "Rhino", "White Rhino", "Hluhluwe",
            "Ranger R", 3, 1000, 2, 500, false
        );
        manager.addCase(r1);
        manager.addCase(r2);

        double expected = (2 * 1000 + 2000) + (3 * 1000 + 500);
        assertEquals(expected, manager.getTotalEstimatedCost(), 0.001);
    }
}
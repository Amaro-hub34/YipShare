package com.startupidea.yipshare.loan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.startupidea.yipshare.condominium.FakeCondominiumRepository;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemAvailability;
import com.startupidea.yipshare.item.ItemNotAvailable;
import com.startupidea.yipshare.item.ItemNotFound;
import com.startupidea.yipshare.item.ItemPayload;

import org.junit.Test;

import java.util.Collections;

import java.util.ArrayList;
import java.util.List;

public class LoanServiceTest {

    private final List<Loan> stored = new ArrayList<>();
    private LoanStatus savedStatus;
    private int demoCalls;
    private Loan requestToAnswer;
    private String listedCondominium;
    private String demoCondominiums;
    private boolean requested;
    private Integer requestedDays;
    private final FakeCondominiumRepository condominiums = new FakeCondominiumRepository();

    private final LoanService service = new LoanService(new LoanRepository() {
        @Override
        public List<Loan> getAllLoansByUserId(String userId, String condominiumId) {
            listedCondominium = condominiumId;
            return new ArrayList<>(stored);
        }

        @Override
        public List<LoanRequest> getAllLoanRequestsByLenderId(String lenderId, String condominiumId) {
            return new ArrayList<>();
        }

        @Override
        public boolean existsLoanByUserId(String userId) {
            return !stored.isEmpty();
        }

        @Override
        public Loan getLoanById(String loanId, String userId) {
            if (requestToAnswer != null) return requestToAnswer;
            return loan(savedStatus);
        }

        @Override
        public void modifyLoanStatus(String loanId, LoanStatus status) {
            savedStatus = status;
        }

        @Override
        public boolean existsPendingRequest(String userId, String articleId) {
            return requested;
        }

        @Override
        public void createLoanRequest(String lenderId, String borrowerId, String articleId, String requestText, int days) {
            requestedDays = days;
        }

        @Override
        public void createDemoLoans(String userId, String administeredId, String livedId) {
            demoCalls++;
            demoCondominiums = administeredId + "|" + livedId;
            stored.add(loan(LoanStatus.ACCEPTED));
        }
    }, condominiums);

    private static Loan loan(LoanStatus status) {
        return new Loan("loan", "Drill", "Works well", null, "other", "ana", true, null, "08/11/2026", status, "+525500000000");
    }

    @Test
    public void firstCodeStartsTheLoan() {
        Loan updated = service.confirmCode("user", loan(LoanStatus.ACCEPTED), "123456");

        assertEquals(LoanStatus.ON_LOAN, savedStatus);
        assertEquals(LoanStatus.ON_LOAN, updated.getStatus());
    }

    @Test
    public void secondCodeFinishesTheLoan() {
        service.confirmCode("user", loan(LoanStatus.ON_LOAN), "123456");

        assertEquals(LoanStatus.FINISHED, savedStatus);
    }

    @Test
    public void malformedCodeKeepsTheStatus() {
        assertThrows(InvalidCode.class, () -> service.confirmCode("user", loan(LoanStatus.ACCEPTED), "12a45"));
        assertNull(savedStatus);
    }

    @Test
    public void pendingOrFinishedLoansTakeNoCode() {
        assertThrows(LoanNotActive.class, () -> service.confirmCode("user", loan(LoanStatus.PENDING), "123456"));
        assertThrows(LoanNotActive.class, () -> service.confirmCode("user", loan(LoanStatus.FINISHED), "123456"));
        assertNull(savedStatus);
    }

    @Test
    public void createsDemoLoansOnlyWhenTheUserHasNone() {
        assertEquals(1, service.getAllLoansByUserId("user", "condo").size());
        assertEquals(1, service.getAllLoansByUserId("user", "condo").size());
        assertEquals(1, demoCalls);
        // The demo loans go to the user's demo condominiums, created first if needed.
        assertEquals(1, condominiums.demoCalls);
        assertEquals("demo|demo", demoCondominiums);
    }

    @Test
    public void statusMatchesTheDatabaseValues() {
        assertEquals("on_loan", LoanStatus.ON_LOAN.getValue());
        assertEquals(LoanStatus.ON_LOAN, LoanStatus.parse("on_loan"));
    }

    @Test
    public void lenderAcceptsOrRejectsPendingRequest() {
        requestToAnswer = loan(LoanStatus.PENDING);
        service.acceptLoanRequest("user", "loan");
        assertEquals(LoanStatus.ACCEPTED, savedStatus);

        service.rejectLoanRequest("user", "loan");
        assertEquals(LoanStatus.REJECTED, savedStatus);
    }

    @Test
    public void requestIsAnsweredOnlyOnceAndOnlyByTheLender() {
        requestToAnswer = loan(LoanStatus.ACCEPTED);
        assertThrows(LoanRequestNotPending.class, () -> service.rejectLoanRequest("user", "loan"));

        requestToAnswer = new Loan("loan", "Drill", "d", null, "other", "ana", false, null, "08/11/2026", LoanStatus.PENDING, "+525500000000");
        assertThrows(LoanRequestNotPending.class, () -> service.acceptLoanRequest("user", "loan"));
        assertNull(savedStatus);
    }

    private static Item item(String ownerId, ItemAvailability availability) {
        return new Item("item", ownerId, "condo", availability,
                new ItemPayload("Drill", "Tools", "d", "Good", null, Collections.singletonList("1 day")));
    }

    @Test
    public void neighborRequestsAvailableItem() {
        condominiums.members.add(FakeCondominiumRepository.key("user", "condo"));

        service.requestLoan("user", item("owner", ItemAvailability.AVAILABLE), "text", 7);

        assertEquals(Integer.valueOf(7), requestedDays);
    }

    @Test
    public void neighborWithoutLoanAccessCannotRequest() {
        condominiums.members.add(FakeCondominiumRepository.key("user", "condo"));
        condominiums.withoutLoanAccess.add(FakeCondominiumRepository.key("user", "condo"));

        assertThrows(LoanAccessRevoked.class, () -> service.requestLoan("user", item("owner", ItemAvailability.AVAILABLE), "t", 1));
        assertNull(requestedDays);
    }

    @Test
    public void cannotRequestOwnLentOrAlreadyRequestedItems() {
        condominiums.members.add(FakeCondominiumRepository.key("user", "condo"));

        assertThrows(ItemNotFound.class, () -> service.requestLoan("user", item("user", ItemAvailability.AVAILABLE), "t", 1));
        assertThrows(ItemNotAvailable.class, () -> service.requestLoan("user", item("owner", ItemAvailability.NOT_AVAILABLE), "t", 1));
        requested = true;
        assertThrows(AlreadyRequested.class, () -> service.requestLoan("user", item("owner", ItemAvailability.AVAILABLE), "t", 1));
        assertNull(requestedDays);
    }

    @Test
    public void listsOnlyTheSelectedCondominium() {
        stored.add(loan(LoanStatus.ACCEPTED));

        service.getAllLoansByUserId("user", "condo");
        assertEquals("condo", listedCondominium);

        listedCondominium = null;
        assertTrue(service.getAllLoansByUserId("user", null).isEmpty());
        assertTrue(service.getAllLoanRequests("user", null).isEmpty());
        assertNull(listedCondominium);
    }
}

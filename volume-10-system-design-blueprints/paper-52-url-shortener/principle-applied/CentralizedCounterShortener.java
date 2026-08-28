// Every shorten() call goes straight to the central counter -- correct,
// collision-free by construction, but every single request now pays the
// central service's network cost.
class CentralizedCounterShortener {
    private final CentralIdService idService;

    CentralizedCounterShortener(CentralIdService idService) {
        this.idService = idService;
    }

    String shorten() {
        long id = idService.nextId();
        return Base62.encode(id);
    }
}

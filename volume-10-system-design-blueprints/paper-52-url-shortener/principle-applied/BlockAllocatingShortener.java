// Represents one application node. Fetches a BLOCK of IDs from the central
// service in one round trip, then hands them out locally with no network
// cost at all until the block runs out -- and no other node can ever be
// handed the same block, so IDs still never collide.
class BlockAllocatingShortener {
    private static final int BLOCK_SIZE = 1000;

    private final CentralIdService idService;
    private long blockStart;
    private long nextInBlock;
    private long blockEnd;

    BlockAllocatingShortener(CentralIdService idService) {
        this.idService = idService;
        fetchNewBlock();
    }

    synchronized String shorten() {
        if (nextInBlock >= blockEnd) {
            fetchNewBlock();
        }
        long id = nextInBlock++;
        return Base62.encode(id);
    }

    private void fetchNewBlock() {
        blockStart = idService.allocateBlock(BLOCK_SIZE);
        nextInBlock = blockStart;
        blockEnd = blockStart + BLOCK_SIZE;
    }
}

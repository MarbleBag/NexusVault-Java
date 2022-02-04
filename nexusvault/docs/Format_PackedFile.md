# Format

## Block

Each block of data, except for the header, is aligned to a 16-byte boundary and surrounded by two 64bit signed integer, Block-Guards, which are also 16 byte aligned. Both Block-Guards are equal to the number of bytes which lie in between both Block-Guards, that number will technically be equal to the size of the block plus any padding that is needed to align said Block-Guards correctly to a 16-byte boundary.
Except for the start and end of the file, a block-guard will be immediately followed by the next block-guard of the next block.
This allows iterating, block-by-block, through the file, in both directions.

It seems, if, and only if, a negative block-guard indicates that the following block is not used and is, probably, free to be reused. (As seen in .archive)
The size of the block is equal to the absolute value of the block-guard.

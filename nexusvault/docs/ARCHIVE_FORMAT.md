# Format

## Block

Each block of data, except for the header, is aligned to a 16-byte boundary and surrounded by two 64bit signed integer, Block-Guards, which are also 16 byte aligned. Both Block-Guards are equal to the number of bytes which lie in between both Block-Guards, that number will technically be equal to the size of the block plus any padding that is needed to align said Block-Guards correctly to a 16-byte boundary.
Except for the start and end of the file, a block-guard will be immediately followed by the next block-guard of the next block.
This allows iterating, block-by-block, through the file, in both directions.

It seems, if, and only if, a block-guard is negative, it indicates that the block it surrounds is not used and is probably free to be reused.
The size of the block is equal to the absolute value of the block-guard.

The exact reason why those Block-Guards exist is not clear.

### Example:
A block is located at 0x290 and its size is 126 byte, that means the block spans from 0x290 to 0x30E.
The first guard is located at 0x288 (8byte int!), but the second guard is not located at 0x30E, because 0x30E is not aligned
The next correctly aligned position is 0x310 (0x310 divided by 16 is an integer). The distance between 0x290 and 0x310 is equal to 128 bytes.
This means the value of both guards is 128.

If the first, or the last, guard is read, it is known how many bytes need to be skipped to reach the other guard on the other side of the block.
// SPDX-License-Identifier: MIT
pragma solidity >= 0.8.7;

// Structs whose fields are static arrays of dynamic types (bytes[2], string[2], uint256[][2])
// or nested static arrays (uint256[2][2]), plus nested dynamic arrays (uint256[][]).
contract StaticArraysInStruct {
    struct Pair {
        bytes[2] blobs;
        uint256 n;
    }

    struct Square {
        uint256[2][2] cells;
        uint256 n;
    }

    struct Labels {
        string[2] names;
        uint256[][2] groups;
        uint256 n;
    }

    event PairEvent(Pair p, uint256[][] grid);

    function pair(Pair calldata p) external pure returns (Pair memory) {
        return p;
    }

    function square(Square calldata s) external pure returns (Square memory) {
        return s;
    }

    function labels(Labels calldata l) external pure returns (Labels memory, uint256[][] memory grid) {
        grid = new uint256[][](1);
        return (l, grid);
    }

    function firePair(Pair calldata p) external {
        emit PairEvent(p, new uint256[][](0));
    }
}

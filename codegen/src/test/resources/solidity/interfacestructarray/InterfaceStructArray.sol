// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.20;


interface IERC721CollectionV2 {

    struct ItemParam {
        string rarity;
        uint256 price;
        address beneficiary;
        string metadata;
    }

    function initialize(
        string memory _name,
        string memory _symbol,
        string memory _baseURI,
        address _creator,
        bool _shouldComplete,
        bool _isApproved,
        address _rarities,
        ItemParam[] memory _items
    ) external;
}

pragma solidity ^0.8.4;

contract CustomError {
    address public _testAddress = 0x9A734f85fE7676096979503f8CEd26EA387138b4;

    error InvalidAccess(address, string reason);

    function customError() public {
        revert InvalidAccess(_testAddress, "wrong address");
    }
}

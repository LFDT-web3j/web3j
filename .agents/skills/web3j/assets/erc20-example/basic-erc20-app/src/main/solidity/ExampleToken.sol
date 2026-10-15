// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.8.20;

contract ExampleToken {
    string public name;
    string public symbol;
    uint8 public constant decimals = 18;
    uint256 public totalSupply;

    mapping(address => uint256) public balanceOf;
    mapping(address => mapping(address => uint256)) public allowance;

    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);

    constructor(
        uint256 initialSupply,
        string memory tokenName,
        string memory tokenSymbol
    ) {
        name = tokenName;
        symbol = tokenSymbol;
        totalSupply = initialSupply;
        balanceOf[msg.sender] = initialSupply;

        emit Transfer(address(0), msg.sender, initialSupply);
    }

    function transfer(address to, uint256 value) external returns (bool) {
        require(to != address(0), "recipient required");
        require(balanceOf[msg.sender] >= value, "insufficient balance");

        balanceOf[msg.sender] -= value;
        balanceOf[to] += value;

        emit Transfer(msg.sender, to, value);
        return true;
    }

    function approve(address spender, uint256 value) external returns (bool) {
        require(spender != address(0), "spender required");

        allowance[msg.sender][spender] = value;
        emit Approval(msg.sender, spender, value);
        return true;
    }

    function transferFrom(
        address from,
        address to,
        uint256 value
    ) external returns (bool) {
        require(to != address(0), "recipient required");
        require(balanceOf[from] >= value, "insufficient balance");
        require(allowance[from][msg.sender] >= value, "insufficient allowance");

        allowance[from][msg.sender] -= value;
        balanceOf[from] -= value;
        balanceOf[to] += value;

        emit Transfer(from, to, value);
        return true;
    }
}

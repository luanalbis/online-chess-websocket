package com.luanalbischess.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {
	Position source;
	Position target;
}

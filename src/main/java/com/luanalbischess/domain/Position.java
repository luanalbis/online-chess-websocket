package com.luanalbischess.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Position {
	private Integer row;
	private Integer column;

	public void setValues(Integer row, Integer column) {
		this.row = row;
		this.column = column;

	}
}

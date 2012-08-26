package com.moosemanstudios.MooseList;

import com.alta189.simplesave.Field;
import com.alta189.simplesave.Id;
import com.alta189.simplesave.Table;

@Table(name = "sqltable")
public class sqlTable {
	@Id
	public int id;
	
	@Field
	public String player;
	
	
}

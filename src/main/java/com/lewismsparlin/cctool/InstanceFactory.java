package com.lewismsparlin.cctool;

import com.lewismsparlin.cctool.api.HtmlTransformer;
import com.lewismsparlin.cctool.transform.PsToCcTransformer;

public class InstanceFactory {

	public static HtmlTransformer transformer() {
		return new PsToCcTransformer();
	}

}

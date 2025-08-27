package com.garganttua.api.spec.entity;

public record EntityDocumentationInfos (
		String general,
		String readAll,
		String readOne,
		String createOne,
		String updateOne,
		String deleteOne,
		String deleteAll
		) {

}

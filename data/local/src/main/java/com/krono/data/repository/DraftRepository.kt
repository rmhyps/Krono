package com.krono.data.repository

import com.krono.data.database.KronoDao
import com.krono.data.entity.DraftAssetRefEntity
import com.krono.data.entity.RuleDraftEntity

class DraftRepository(private val dao: KronoDao) {

    suspend fun saveDraft(draft: RuleDraftEntity, assetIds: List<String> = emptyList()) {
        dao.saveDraft(draft)
    }

    suspend fun getDraft(draftId: String): RuleDraftEntity? {
        return dao.getDraft(draftId)
    }

    suspend fun deleteDraft(draftId: String) {
        dao.deleteDraft(draftId)
    }
}

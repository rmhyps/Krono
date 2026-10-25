package com.krono.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.krono.data.entity.ActionAttemptEntity
import com.krono.data.entity.ActionEntity
import com.krono.data.entity.ActionHistoryEntity
import com.krono.data.entity.AssetEntity
import com.krono.data.entity.AssetReadLeaseEntity
import com.krono.data.entity.BranchEntity
import com.krono.data.entity.CommandReceiptEntity
import com.krono.data.entity.DraftAssetRefEntity
import com.krono.data.entity.EngineMetaEntity
import com.krono.data.entity.EpisodeClaimEntity
import com.krono.data.entity.EvaluationBatchEntity
import com.krono.data.entity.EvaluationRoundEntity
import com.krono.data.entity.EventInboxEntity
import com.krono.data.entity.EventSourceEntity
import com.krono.data.entity.ExecutionHistoryEntity
import com.krono.data.entity.MonitoringStateEntity
import com.krono.data.entity.OpportunityEntity
import com.krono.data.entity.OutboxEntity
import com.krono.data.entity.PlanEntity
import com.krono.data.entity.PlanTargetEntity
import com.krono.data.entity.RoundDecisionEntity
import com.krono.data.entity.RuleDraftEntity
import com.krono.data.entity.RuleEntity
import com.krono.data.entity.RuleEpisodeStateEntity
import com.krono.data.entity.RulePriorityEntity
import com.krono.data.entity.RuleRevisionEntity
import com.krono.data.entity.RuleSourceEntity
import com.krono.data.entity.RuleStateEntity
import com.krono.data.entity.RuntimeSessionEntity
import com.krono.data.entity.SourceDefinitionEntity
import com.krono.data.entity.SourceStateEntity
import com.krono.data.entity.TargetHeadEntity

@Database(
    entities = [
        RuleEntity::class,
        RuleRevisionEntity::class,
        RuleStateEntity::class,
        RulePriorityEntity::class,
        BranchEntity::class,
        ActionEntity::class,
        RuleSourceEntity::class,
        SourceDefinitionEntity::class,
        RuleDraftEntity::class,
        DraftAssetRefEntity::class,
        AssetEntity::class,
        AssetReadLeaseEntity::class,
        RuntimeSessionEntity::class,
        EngineMetaEntity::class,
        MonitoringStateEntity::class,
        SourceStateEntity::class,
        RuleEpisodeStateEntity::class,
        CommandReceiptEntity::class,
        EventInboxEntity::class,
        EventSourceEntity::class,
        EvaluationBatchEntity::class,
        EvaluationRoundEntity::class,
        OpportunityEntity::class,
        RoundDecisionEntity::class,
        EpisodeClaimEntity::class,
        PlanEntity::class,
        PlanTargetEntity::class,
        TargetHeadEntity::class,
        ActionAttemptEntity::class,
        OutboxEntity::class,
        ExecutionHistoryEntity::class,
        ActionHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class KronoDatabase : RoomDatabase() {

    abstract fun kronoDao(): KronoDao

    companion object {
        const val DATABASE_NAME = "krono.db"

        @Volatile
        private var instance: KronoDatabase? = null

        fun getInstance(context: Context): KronoDatabase {
            return instance ?: synchronized(this) {
                instance ?: create(context.applicationContext, inMemory = false).also { instance = it }
            }
        }

        fun create(context: Context, inMemory: Boolean = false): KronoDatabase {
            val builder = if (inMemory) {
                Room.inMemoryDatabaseBuilder(context, KronoDatabase::class.java)
            } else {
                Room.databaseBuilder(context, KronoDatabase::class.java, DATABASE_NAME)
            }
            return builder
                .setJournalMode(JournalMode.TRUNCATE)
                .build()
        }
    }
}

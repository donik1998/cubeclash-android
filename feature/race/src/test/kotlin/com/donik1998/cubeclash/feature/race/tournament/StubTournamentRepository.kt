package com.donik1998.cubeclash.feature.race.tournament

import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.TournamentRepository
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail

/**
 * A queued-results test double, mirroring `StubUserRepository` in the stats tests: each of the three
 * methods returns its supplied results once per call in order, holding the last one thereafter — so a
 * fail-then-succeed sequence exercises error-then-retry with one stub, and a register-then-refresh
 * sequence can hand back a fresh detail. Counters let a test assert how many times each was called.
 */
class StubTournamentRepository(
    private val listResults: List<DataResult<List<Tournament>>> = emptyList(),
    private val detailResults: List<DataResult<TournamentDetail>> = emptyList(),
    private val registerResults: List<DataResult<Unit>> = emptyList(),
) : TournamentRepository {

    var listCalls = 0
        private set
    var detailCalls = 0
        private set
    var registerCalls = 0
        private set

    override suspend fun tournaments(): DataResult<List<Tournament>> {
        val result = listResults[listCalls.coerceAtMost(listResults.lastIndex)]
        listCalls++
        return result
    }

    override suspend fun tournament(id: String): DataResult<TournamentDetail> {
        val result = detailResults[detailCalls.coerceAtMost(detailResults.lastIndex)]
        detailCalls++
        return result
    }

    override suspend fun register(id: String): DataResult<Unit> {
        val result = registerResults[registerCalls.coerceAtMost(registerResults.lastIndex)]
        registerCalls++
        return result
    }
}

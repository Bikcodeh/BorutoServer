package com.bikcode

import com.bikcode.models.ApiResponse
import com.bikcode.repository.HeroRepository
import com.bikcode.repository.NEXT_PAGE_KEY
import com.bikcode.repository.PREV_PAGE_KEY
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.application.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

class ApplicationTest {

    private val heroRepository: HeroRepository by inject(HeroRepository::class.java)

    @Test
    fun `access root endpoint assert correct information`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )
                assertEquals(
                    expected = "Welcome to Boruto server",
                    actual = response.content
                )
            }
        }
    }

    @Test
    fun `access all heroes endpoint assert correct information`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )
                val actual = Json.decodeFromString<ApiResponse>(response.content.toString())
                val expected = ApiResponse(
                    success = true,
                    message = "OK",
                    prevPage = null,
                    nextPage = 2,
                    heroes = heroRepository.page1,
                    lastUpdated = actual.lastUpdated
                )
                assertEquals(
                    expected = expected,
                    actual = actual
                )
            }
        }
    }

    @Test
    fun `access all heroes endpoint, query second page assert correct information`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=2").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status()
                )
                val actual = Json.decodeFromString<ApiResponse>(response.content.toString())
                val expected = ApiResponse(
                    success = true,
                    message = "OK",
                    prevPage = 1,
                    nextPage = 3,
                    heroes = heroRepository.page2,
                    lastUpdated = actual.lastUpdated
                )
                assertEquals(
                    expected = expected,
                    actual = actual
                )
            }
        }
    }

    @Test
    fun `access all heroes endpoint, query all pages assert correct information`() {
        withTestApplication(moduleFunction = Application::module) {
            val pages = 1..5
            val heroes = listOf(
                heroRepository.page1,
                heroRepository.page2,
                heroRepository.page3,
                heroRepository.page4,
                heroRepository.page5
            )

            pages.forEach { page ->
                handleRequest(HttpMethod.Get, "/boruto/heroes?page=$page").apply {
                    assertEquals(
                        expected = HttpStatusCode.OK,
                        actual = response.status()
                    )
                    val actual = Json.decodeFromString<ApiResponse>(response.content.toString())
                    val expected = ApiResponse(
                        success = true,
                        message = "OK",
                        prevPage = calculatePage(page)["prevPage"],
                        nextPage = calculatePage(page)["nextPage"],
                        heroes = heroes[page - 1],
                        lastUpdated = actual.lastUpdated

                    )
                    assertEquals(
                        expected = expected,
                        actual = actual
                    )
                }
            }
        }
    }
    private fun calculatePage(page: Int): Map<String, Int?> {
        var prevPage: Int? = page
        var nextPage: Int? = page

        if (page in 1..4) {
            nextPage = nextPage?.plus(1)
        }

        if (page in 2..5) {
            prevPage = prevPage?.minus(1)
        }

        if (page == 1) {
            prevPage = null
        }

        if (page == 5) {
            nextPage = null
        }
        return mapOf(PREV_PAGE_KEY to prevPage, NEXT_PAGE_KEY to nextPage)
    }

    @Test
    fun `access all heroes endpoint, query non existing page number, assert error`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=6").apply {
                val expected = ApiResponse(
                    success = false,
                    message = "Heroes not found"
                )
                assertEquals(
                    expected = expected,
                    actual = decodeJson(response.content.toString())
                )

                assertEquals(
                    expected = HttpStatusCode.NotFound,
                    actual = response.status()
                )
            }
        }
    }
    @Test
    fun `access all heroes endpoint, page not number, assert error`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=nan").apply {
                val expected = ApiResponse(
                    success = false,
                    message = "Only numbers allowed"
                )
                assertEquals(
                    expected = expected,
                    actual = decodeJson(response.content.toString())
                )


                assertEquals(
                    expected = HttpStatusCode.BadRequest,
                    actual = response.status()
                )
            }
        }
    }

    @Test
    fun `access search heroes endpoint, query hero name, assert single hero result`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=sas").apply {
                assertEquals(expected = HttpStatusCode.OK, actual = response.status())
                val actual = decodeJson(response.content.toString()).heroes.count()
                assertEquals(expected = 1, actual = actual)
            }
        }
    }

    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes result`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=sa").apply {
                assertEquals(expected = HttpStatusCode.OK, actual = response.status())
                val actual = decodeJson(response.content.toString()).heroes.count()
                assertEquals(expected = 3, actual = actual)
            }
        }
    }

    @Test
    fun `access search heroes endpoint, query an empty hero name, assert empty list as a result`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=").apply {
                assertEquals(expected = HttpStatusCode.OK, actual = response.status())
                val actual = decodeJson(response.content.toString()).heroes
                assertEquals(expected = emptyList(), actual = actual)
            }
        }
    }

    @Test
    fun `access search heroes endpoint, query non existing hero name, assert empty list as a result`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=nan").apply {
                assertEquals(expected = HttpStatusCode.OK, actual = response.status())
                val actual = decodeJson(response.content.toString()).heroes
                assertEquals(expected = emptyList(), actual = actual)
            }
        }
    }

    @Test
    fun `access non existing endpoint, assert not found`() {
        withTestApplication(moduleFunction = Application::module) {
            handleRequest(HttpMethod.Get, "/unknown").apply {
                assertEquals(expected = HttpStatusCode.NotFound, actual = response.status())
                assertEquals(expected = "Page not found", actual = response.content)
            }
        }
    }

    private fun decodeJson(json: String): ApiResponse = Json.decodeFromString<ApiResponse>(json)
}
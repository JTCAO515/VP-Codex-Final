import unittest

from tests.test_support import request


class ApiContractTests(unittest.TestCase):
    def test_cities_are_clean_public_records(self):
        code, data, _ = request("GET", "/api/cities")
        self.assertEqual(code, 200)
        self.assertGreater(data["count"], 20)
        first = data["cities"][0]
        self.assertIn("name", first)
        self.assertNotIn("\u951b", " ".join(first["highlights"]))

        code, detail, _ = request("GET", "/api/cities/beijing")
        self.assertEqual(code, 200)
        self.assertEqual(detail["city"]["name"], "Beijing")

    def test_tools_and_visa_endpoints(self):
        code, tools, _ = request("GET", "/api/tools")
        self.assertEqual(code, 200)
        self.assertGreaterEqual(tools["count"], 4)

        code, detail, _ = request("GET", "/api/tools/packing")
        self.assertEqual(code, 200)
        self.assertTrue(detail["tool"]["items"][0]["required"])

        code, visa, _ = request("GET", "/api/visa/info", query="nationality=us")
        self.assertEqual(code, 200)
        self.assertEqual(visa["policy"]["country"], "United States")

    def test_chat_streams_server_sent_events(self):
        code, body, headers = request("POST", "/api/chat", {
            "message": "Plan Beijing and Chengdu for 7 days",
            "mode": "itinerary",
            "provider": "local-guide",
            "depth": "expert",
        })
        self.assertEqual(code, 200)
        self.assertIn("text/event-stream", headers["Content-Type"])
        self.assertIn("data:", body)
        self.assertIn("providerLabel", body)
        self.assertIn("Beijing", body)

    def test_remote_chat_timeout_leaves_room_for_frontend_fallback(self):
        from api import chat

        self.assertLessEqual(chat.REMOTE_MODEL_TIMEOUT_SECONDS, 10)

    def test_chat_options_expose_modes_and_routes(self):
        code, data, _ = request("GET", "/api/chat")
        self.assertEqual(code, 200)
        self.assertIn("itinerary", {mode["id"] for mode in data["modes"]})
        self.assertIn("local-guide", {provider["id"] for provider in data["providers"]})
        self.assertIn("expert", {depth["id"] for depth in data["depths"]})

    def test_translation_library_exposes_travel_butler_data(self):
        code, data, _ = request("GET", "/api/translations")
        self.assertEqual(code, 200)
        self.assertIn("phrases", data)
        self.assertIn("dining", data)
        self.assertIn("attractions", data)
        self.assertIn("culture", data)
        self.assertGreaterEqual(len(data["phrases"]["phrases"]), 4)
        self.assertGreaterEqual(len(data["dining"]["dishes"]), 4)
        self.assertGreaterEqual(len(data["attractions"]["attractions"]), 4)

    def test_maps_hotels_and_deals_butler_endpoints(self):
        code, geocode, _ = request("GET", "/api/maps/geocode", query="q=北京故宫")
        self.assertEqual(code, 200)
        self.assertEqual(geocode["query"], "北京故宫")
        self.assertIn("lat", geocode["location"])
        self.assertIn("lng", geocode["location"])

        code, places, _ = request("GET", "/api/maps/place", query="type=hotel&lat=39.916&lng=116.397")
        self.assertEqual(code, 200)
        self.assertEqual(places["type"], "hotel")
        self.assertGreaterEqual(len(places["places"]), 2)

        code, translated, _ = request("GET", "/api/maps/translate", query="name=故宫")
        self.assertEqual(code, 200)
        self.assertEqual(translated["english"], "Forbidden City")
        self.assertIn("pinyin", translated)

        code, hotels, _ = request("GET", "/api/hotels/search", query="city=北京&checkin=2026-06-28&checkout=2026-06-30")
        self.assertEqual(code, 200)
        self.assertGreaterEqual(len(hotels["hotels"]), 2)
        self.assertTrue(hotels["hotels"][0]["foreignerFriendly"]["acceptsForeignGuests"])

        code, detail, _ = request("GET", "/api/hotels/detail", query=f"id={hotels['hotels'][0]['id']}")
        self.assertEqual(code, 200)
        self.assertIn("metroDistance", detail["hotel"])

        code, deals, _ = request("GET", "/api/deals/search", query="city=北京&type=food")
        self.assertEqual(code, 200)
        self.assertGreaterEqual(len(deals["deals"]), 2)
        self.assertIn("foreignerUsability", deals["deals"][0])

        code, deal_detail, _ = request("GET", "/api/deals/detail", query=f"id={deals['deals'][0]['id']}")
        self.assertEqual(code, 200)
        self.assertIn("englishGuide", deal_detail["deal"]["foreignerUsability"])


if __name__ == "__main__":
    unittest.main()

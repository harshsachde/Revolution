#!/usr/bin/env python3
"""
Find cheapest flights for a given route/date using Kiwi Tequila API.

Setup:
  export TEQUILA_API_KEY="..."

Example:
  python3 flight_search.py --from BLR --to AMD --date 2026-02-13 --currency INR
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import sys
import urllib.parse
import urllib.request


TEQUILA_SEARCH_URL = "https://api.tequila.kiwi.com/v2/search"


def _iso_to_ddmmyyyy(iso_date: str) -> str:
    d = dt.date.fromisoformat(iso_date)
    return d.strftime("%d/%m/%Y")


def _request_json(url: str, *, headers: dict[str, str], timeout_s: int = 30) -> dict:
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=timeout_s) as resp:
        charset = resp.headers.get_content_charset() or "utf-8"
        body = resp.read().decode(charset, errors="replace")
    return json.loads(body)


def find_cheapest(*, fly_from: str, fly_to: str, date: str, currency: str, adults: int, max_stopovers: int) -> dict:
    api_key = os.getenv("TEQUILA_API_KEY")
    if not api_key:
        raise RuntimeError(
            "Missing TEQUILA_API_KEY. Add it as an environment variable (or Cursor secret) to fetch live prices."
        )

    date_ddmmyyyy = _iso_to_ddmmyyyy(date)
    params = {
        "fly_from": fly_from,
        "fly_to": fly_to,
        "date_from": date_ddmmyyyy,
        "date_to": date_ddmmyyyy,
        "adults": str(adults),
        "curr": currency,
        "limit": "50",
        "sort": "price",
        "max_stopovers": str(max_stopovers),
        "vehicle_type": "aircraft",
    }
    url = TEQUILA_SEARCH_URL + "?" + urllib.parse.urlencode(params)
    headers = {
        "apikey": api_key,
        "accept": "application/json",
        "user-agent": "cheapest-flight-cli/1.0",
    }
    payload = _request_json(url, headers=headers, timeout_s=45)
    data = payload.get("data") or []
    if not data:
        # surface API error details if present
        raise RuntimeError(f"No flights returned. Response keys={list(payload.keys())}.")

    cheapest = min(data, key=lambda x: x.get("price", 10**18))
    return cheapest


def _fmt_money(price: int | float | None, currency: str) -> str:
    if price is None:
        return "N/A"
    try:
        p = int(price)
    except Exception:
        return str(price)
    return f"{currency} {p:,}"


def main(argv: list[str]) -> int:
    p = argparse.ArgumentParser(description="Find cheapest flight using Kiwi Tequila API.")
    p.add_argument("--from", dest="fly_from", required=True, help="Origin IATA (e.g., BLR)")
    p.add_argument("--to", dest="fly_to", required=True, help="Destination IATA (e.g., AMD)")
    p.add_argument("--date", required=True, help="Travel date in ISO format (YYYY-MM-DD)")
    p.add_argument("--currency", default="INR", help="Currency code (default: INR)")
    p.add_argument("--adults", type=int, default=1, help="Number of adults (default: 1)")
    p.add_argument("--max-stopovers", type=int, default=1, help="Max stopovers (default: 1)")
    args = p.parse_args(argv)

    try:
        f = find_cheapest(
            fly_from=args.fly_from.upper().strip(),
            fly_to=args.fly_to.upper().strip(),
            date=args.date.strip(),
            currency=args.currency.upper().strip(),
            adults=args.adults,
            max_stopovers=args.max_stopovers,
        )
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        return 2

    price = f.get("price")
    deep_link = f.get("deep_link") or ""
    airlines = f.get("airlines") or []
    route = f.get("route") or []

    print(f"Cheapest: {_fmt_money(price, args.currency.upper())}")
    if airlines:
        print("Airlines:", ", ".join(airlines))
    if route:
        first = route[0]
        last = route[-1]
        print("Depart:", first.get("local_departure") or first.get("utc_departure") or "N/A")
        print("Arrive:", last.get("local_arrival") or last.get("utc_arrival") or "N/A")
        if len(route) > 1:
            stops = len(route) - 1
            print(f"Stops: {stops}")
        for i, seg in enumerate(route, start=1):
            fr = f"{seg.get('flyFrom')} ({seg.get('cityFrom')})"
            to = f"{seg.get('flyTo')} ({seg.get('cityTo')})"
            al = seg.get("airline") or "?"
            fn = seg.get("flight_no") or "?"
            dep = seg.get("local_departure") or "N/A"
            arr = seg.get("local_arrival") or "N/A"
            print(f"Leg {i}: {fr} -> {to} | {al}{fn} | {dep} -> {arr}")
    if deep_link:
        print("Booking link:", deep_link)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))


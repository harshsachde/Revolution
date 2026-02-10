# Revolution

This repo currently includes a small CLI to fetch the **cheapest flight** for a route/date.

## Cheapest flight CLI (live prices)

The CLI uses the **Kiwi Tequila API** (requires an API key).

- **Set API key** (recommended: add a Cursor Secret named `TEQUILA_API_KEY`):

```bash
export TEQUILA_API_KEY="your_key_here"
```

- **Run** (Bengaluru → Ahmedabad, 13 Feb 2026):

```bash
python3 flight_search.py --from BLR --to AMD --date 2026-02-13 --currency INR --max-stopovers 1
```

It prints the cheapest itinerary it finds (price, legs, and booking link).

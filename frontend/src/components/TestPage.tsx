import React, { useState } from "react";
import { ApiService } from "../services/api";

const TestPage: React.FC = () => {
  const [query, setQuery] = useState("");
  const [context, setContext] = useState("");
  const [ragResult, setRagResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRAGTest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim() || !context.trim()) return;

    setLoading(true);
    setError(null);
    setRagResult(null);

    try {
      const result = await ApiService.queryRAG(query, context);
      setRagResult(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to query RAG");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md p-6">
        <h1 className="text-2xl font-bold mb-6">API Test Page</h1>

        {/* RAG Test Form */}
        <div className="mb-8">
          <h2 className="text-xl font-semibold mb-4">RAG Query Test</h2>
          <form onSubmit={handleRAGTest} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Query
              </label>
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="What is Tesla's competitive advantage?"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Company Name (Context)
              </label>
              <input
                type="text"
                value={context}
                onChange={(e) => setContext(e.target.value)}
                placeholder="Tesla"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50"
            >
              {loading ? "Querying..." : "Test RAG"}
            </button>
          </form>
        </div>

        {/* Error Display */}
        {error && (
          <div className="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
            <strong>Error:</strong> {error}
          </div>
        )}

        {/* RAG Results */}
        {ragResult && (
          <div className="mt-6">
            <h3 className="text-lg font-semibold mb-2">RAG Result:</h3>
            <pre className="bg-gray-100 p-4 rounded-md overflow-x-auto text-sm">
              {JSON.stringify(ragResult, null, 2)}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};

export default TestPage;

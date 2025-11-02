import Foundation

final class IGolfNetworkClient {
  private static let host = URL(string: "https://api-connect.igolf.com/rest/action/")!

  private let session: URLSession

  init(session: URLSession = IGolfNetworkClient.makeSession()) {
    self.session = session
  }

  func sendRequest(
    endpoint: String,
    body: [String: Any?],
    apiKey: String,
    secretKey: String,
    completion: @escaping (Result<String, Error>) -> Void
  ) {
    guard let path = IGolfAuth.urlPath(for: endpoint, apiKey: apiKey, secretKey: secretKey),
          let url = URL(string: path, relativeTo: IGolfNetworkClient.host) else {
      completion(.failure(NetworkError.invalidURL))
      return
    }

    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("application/json", forHTTPHeaderField: "Accept")

    let payload = body.reduce(into: [String: Any]()) { result, entry in
      if let value = entry.value {
        result[entry.key] = value
      }
    }

    do {
      request.httpBody = try JSONSerialization.data(withJSONObject: payload, options: [])
    } catch {
      completion(.failure(error))
      return
    }

    let task = session.dataTask(with: request) { data, _, error in
      if let error = error {
        completion(.failure(error))
        return
      }

      guard let data = data else {
        completion(.success(""))
        return
      }

      let responseString = String(data: data, encoding: .utf8) ?? ""
      completion(.success(responseString))
    }
    task.resume()
  }

  private static func makeSession() -> URLSession {
    let configuration = URLSessionConfiguration.default
    configuration.timeoutIntervalForRequest = 40
    configuration.timeoutIntervalForResource = 40
    return URLSession(configuration: configuration)
  }

  enum NetworkError: Error {
    case invalidURL
  }
}

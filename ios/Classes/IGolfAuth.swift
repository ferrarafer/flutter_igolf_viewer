import Foundation
import CommonCrypto

enum IGolfAuth {
  private static let version = "1.1"
  private static let signatureVersion = "2.0"
  private static let signatureMethod = "HmacSHA256"
  private static let responseFormat = "JSON"

  private static let timeFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyMMddHHmmssZZZZ"
    formatter.locale = Locale(identifier: "en_US_POSIX")
    return formatter
  }()

  static func urlPath(for request: String, apiKey: String, secretKey: String) -> String? {
    let partOne = "\(request)/\(apiKey)/\(version)/\(signatureVersion)/\(signatureMethod)/"
    let timestamp = timeFormatter.string(from: Date())
    let partTwo = "\(timestamp)/\(responseFormat)"
    guard let signature = makeSignature(for: partOne + partTwo, secret: secretKey) else {
      return nil
    }
    return partOne + signature + "/" + partTwo
  }

  private static func makeSignature(for input: String, secret: String) -> String? {
    guard let messageData = input.data(using: .utf8),
          let secretData = secret.data(using: .utf8) else {
      return nil
    }

    var digest = Data(count: Int(CC_SHA256_DIGEST_LENGTH))
    digest.withUnsafeMutableBytes { digestBytes in
      messageData.withUnsafeBytes { messageBytes in
        secretData.withUnsafeBytes { secretBytes in
          CCHmac(
            CCHmacAlgorithm(kCCHmacAlgSHA256),
            secretBytes.baseAddress,
            secretData.count,
            messageBytes.baseAddress,
            messageData.count,
            digestBytes.baseAddress
          )
        }
      }
    }

    let base64 = digest.base64EncodedString()
    return base64
      .replacingOccurrences(of: "+", with: "-")
      .replacingOccurrences(of: "/", with: "_")
      .replacingOccurrences(of: "=", with: "")
  }
}
